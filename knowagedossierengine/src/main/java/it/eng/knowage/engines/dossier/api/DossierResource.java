/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.engines.dossier.api;

import it.eng.knowage.engines.dossier.DossierEngineConfig;
import it.eng.knowage.engines.dossier.common.PPTTemplateLoader;
import it.eng.knowage.engines.dossier.rest.client.PPTContentClient;
import it.eng.knowage.engines.dossier.template.DossierTemplate;
import it.eng.knowage.engines.dossier.template.parameter.Parameter;
import it.eng.knowage.engines.dossier.template.parser.DossierTemplateParserFactory;
import it.eng.knowage.engines.dossier.template.parser.DossierTemplateType;
import it.eng.knowage.engines.dossier.template.parser.IDossierTemplateParser;
import it.eng.knowage.engines.dossier.template.parser.xml.DossierTemplateXMLParserException;
import it.eng.spagobi.utilities.engines.rest.AbstractEngineRestService;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/start")
public class DossierResource extends AbstractEngineRestService {

	@Context
	protected HttpServletRequest request;

	public static final String RESOURCE_DOSSIER_EXECUTION_FOLDER = "dossierExecution";
	
	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	public Response test() {

		String jsonTemplate = null;
		HttpServletRequest request = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
		HttpServletResponse response = ResteasyProviderFactory.getContextData(HttpServletResponse.class);
		try {
		String template = getTemplateAsString();

		DossierTemplateParserFactory dtf = new DossierTemplateParserFactory();
		IDossierTemplateParser dossierTemplateParser = dtf.getDossierTemplateParser(DossierTemplateType.XML_STRING);
		DossierTemplate dossierTemplate = dossierTemplateParser.parse(template);
		Map<String, String[]> paramMap = request.getParameterMap();

		Integer documentId = Integer.parseInt(getDocumentId());

		List<Parameter> dossierDinamicParams = dossierTemplate.getDinamicParams();

		Iterator<Parameter> it = dossierDinamicParams.iterator();
		while (it.hasNext()) {
			Parameter param = it.next();
			String[] values = paramMap.get(param.getDossierUrlName());
			if (values != null) {
				param.setValue(values[0]);
			}
		}

		

		ObjectMapper mapper = new ObjectMapper();

		
			jsonTemplate = mapper.writeValueAsString(dossierTemplate);
		

		request.setAttribute("jsonTemplate", jsonTemplate);
		request.setAttribute("documentId", documentId);
		request.getParameterMap();

		
			request.getRequestDispatcher("/WEB-INF/jsp/dossier.jsp").forward(request, response);
			
			
		} catch (DossierTemplateXMLParserException e) {
			logger.error(e.getMessage(), e);
			StringBuilder sb = new StringBuilder();
			sb.append("XML Template has errors. Please check folowing:");
			sb.append(System.getProperty("line.separator"));
			sb.append(e.getMessage());
			String message = "XML Template has errors. Please check folowing:\n";
			return Response.serverError().entity(sb.toString()).build();
			//throw new SpagoBIRestServiceException(getLocale(), e);
			
		}catch (ServletException | IOException e) {
			logger.error("error parsing XML dossier template", e);
			
			throw new SpagoBIRestServiceException(getLocale(), e);
			
		}
		return null;
		
	}
	@GET
	@Path("/errorFile")
	public Response getErrorFile(@QueryParam("templateName") String templateName, @QueryParam("randomKey") String randomKey,
			@QueryParam("activityId") Integer activityId, @QueryParam("activityName") String activityName) {
		
		byte[] bytes;
		Map<String, File> files;
		PPTContentClient pptContentClient;
		ResponseBuilder responseBuilder;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		String formattedDate = format.format(cal.getTime());
		
		files  = getPDFs(randomKey);
		File file =null;
		
		pptContentClient = new PPTContentClient();
		for (Map.Entry<String, File> entry : files.entrySet())
		{
			file = entry.getValue();
		    if(file.getName().contains(".txt")){
		    	break;
		    }
		}
		try {
			
			bytes = Files.readAllBytes(file.toPath());
			pptContentClient.storePPT(activityId, bytes, getUserId(), null);
			removePDFDierctory(randomKey);
			
			// return the pptx
			responseBuilder = Response.ok(bytes);

			responseBuilder.header("Content-Disposition", "attachment; filename="+activityName+"_"+formattedDate+".txt");
			responseBuilder.header("filename", activityName+"_"+formattedDate+".txt");
			return responseBuilder.build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	@GET
	@Path("/generatePPT")
	public Response generatePPT(@QueryParam("templateName") String templateName, @QueryParam("randomKey") String randomKey,
			@QueryParam("activityId") Integer activityId, @QueryParam("activityName") String activityName) {

		byte[] bytes;
		PPTTemplateLoader pptLoader;
		File pptTemplate;
		File result;
		PPTContentClient pptContentClient;
		Map<String, File> pdfs;
		ResponseBuilder responseBuilder;
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		String formattedDate = format.format(cal.getTime());

		logger.debug("IN");
		try {
			pptContentClient = new PPTContentClient();
			pptLoader = new PPTTemplateLoader();
			pptTemplate = pptLoader.loadPPTTemplate(templateName);
			pptContentClient = new PPTContentClient();
			if (pptTemplate != null) {
				// get list of pdf files with relative placeholder
				
				pdfs = getPDFs(randomKey);
				
				
				// get images from pdf
				createImages(pdfs, randomKey);
				// insert images inside pptx
				result = createPPT(pptTemplate, pdfs, randomKey);
				bytes = Files.readAllBytes(result.toPath());

				pptContentClient.storePPT(activityId, bytes, getUserId(), null);

				//remove directory of pdfs on server
				removePDFDierctory(randomKey);
				
				// return the pptx
				responseBuilder = Response.ok(bytes);

				responseBuilder.header("Content-Disposition", "attachment; filename="+activityName+"_"+formattedDate+".pptx");
				responseBuilder.header("filename", activityName+"_"+formattedDate+".pptx");
				return responseBuilder.build();
			} else {
				logger.error("Error during generation of PPT, template file is missing");
				throw new SpagoBIRuntimeException("Error during generation of PPT, template file is missing");
			}

		} catch (Exception ex) {
			logger.error("Error while creating output PPT File", ex);
			throw new SpagoBIRuntimeException("Error while creating output PPT File");
		} finally {
			logger.debug("OUT");
		}
		//return Response.noContent().build();

	}
	
	

	// *******************************
	// UTILITY METHODS
	// *******************************

	private Map<String, File> getPDFs(String randomKey) {
		logger.debug("IN");
		Map<String, File> pdfFiles;
		File dossierExecutionFolder = getDossierExecutionFolder();
		String path = dossierExecutionFolder.getAbsolutePath() + File.separator + randomKey;
		File documentsFolder = new File(path);
		if (documentsFolder.exists()) {
			logger.info("Found dossier documents directory for specific execution: " + path);
			pdfFiles = getMapOfFiles(documentsFolder);
		} else {
			logger.error("Cannot find dossier documents directory for specific execution: " + path);
			throw new SpagoBIRuntimeException("Cannot find dossier documents directory for specific execution: " + path);
		}
		logger.debug("OUT");
		return pdfFiles;

	}

	private boolean removePDFDierctory(String randomKey) {
		logger.debug("IN");
		boolean isDeleted = false;
		File dossierExecutionFolder = getDossierExecutionFolder();
		String path = dossierExecutionFolder.getAbsolutePath() + File.separator + randomKey;

		try {
			FileUtils.deleteDirectory(new File(path));
			isDeleted = true;
		} catch (IOException e) {
			logger.error("Error while deleting the temporary directory that contains PDF images for creating the PPT.", e);
		}

		logger.debug("Successfully removed temporary directory for storing PDFs on server!");
		logger.debug("OUT");
		return isDeleted;

	}

	private void createImages(Map<String, File> pdfs, String randomKey) throws IOException {
		File basePath = getDossierExecutionFolder();
		String documentExecutionPath = basePath.getAbsolutePath() + File.separator + randomKey;
		for (Map.Entry<String, File> entry : pdfs.entrySet()) {
			String placeholder = entry.getKey();
			File pdfFile = entry.getValue();
			PDDocument document = PDDocument.load(pdfFile);
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			for (int page = 0; page < document.getNumberOfPages(); ++page) {
				BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 90, ImageType.RGB);
				String imagePath = documentExecutionPath + File.separator + placeholder + File.separator;
				// get image name without extension
				String imageName = pdfFile.getName().replaceFirst("[.][^.]+$", "");
				ImageIOUtil.writeImage(bim, imagePath + (page + 1) + ".png", 90);
			}
			document.close();

		}
	}

	private File createPPT(File pptTemplate, Map<String, File> pdfs, String randomKey) throws IOException {
		FileInputStream inputstream = new FileInputStream(pptTemplate);
		XMLSlideShow ppt = new XMLSlideShow(inputstream);
		XMLSlideShow output_ppt = new XMLSlideShow();

		java.awt.Dimension pgsize = ppt.getPageSize();
		int pgx = pgsize.width; // slide width in points
		int pgy = pgsize.height; // slide height in points

		// set new page size
		output_ppt.setPageSize(new java.awt.Dimension(pgx, pgy));

		File basePath = getDossierExecutionFolder();
		String documentExecutionPath = basePath.getAbsolutePath() + File.separator + randomKey;

		// iterate slides
		for (XSLFSlide slide : ppt.getSlides()) {
			boolean slideDuplicated = false;
			List<XSLFShape> shapes = slide.getShapes();
			for (XSLFShape shape : shapes) {
				logger.debug("Checking shape: " + shape.getShapeName());
				if (shape instanceof XSLFTextShape) {
					String text = ((XSLFTextShape) shape).getText();
					if (text.startsWith("<<") && text.endsWith(">>")) {
						// clean string to get just placeholder name
						text = text.replaceAll("<<", "");
						text = text.replaceAll(">>", "");

						// search if this string is a placeholder of the template
						for (Map.Entry<String, File> entry : pdfs.entrySet()) {
							String placeholders = entry.getKey();
							// placeholder could have multiple values
							StringTokenizer stPlaceholders = new StringTokenizer(placeholders, "-");
							// check every single token separated by "-" char
							while (stPlaceholders.hasMoreElements()) {
								String placeholder = stPlaceholders.nextToken();
								if (placeholder.equalsIgnoreCase(text)) {
									// placeholder found!
									logger.debug("Found placeholder: " + text);
									// access directory (with original name) and read the images

									String imagePath = documentExecutionPath + File.separator + placeholders + File.separator;
									File imageFolder = new File(imagePath);
									File[] pngFiles = imageFolder.listFiles(new FileFilter() {
										@Override
										public boolean accept(File file) {
											return file.isFile() && file.getName().toLowerCase().endsWith(".png");
										}
									});
									// sort the list of files by number
									Arrays.sort(pngFiles, new Comparator<File>() {
										@Override
										public int compare(File f1, File f2) {
											try {
												String file1 = f1.getName().replaceAll(".png", "");
												String file2 = f2.getName().replaceAll(".png", "");
												int i1 = Integer.parseInt(file1);
												int i2 = Integer.parseInt(file2);
												return i1 - i2;
											} catch (NumberFormatException e) {
												throw new AssertionError(e);
											}
										}

									});
									// duplicate slide for each image occurrence
									for (File pngFile : pngFiles) {
										XSLFSlide duplicatedSlide = duplicateSlide(slide, output_ppt);
										slideDuplicated = true;
										insertImageIntoSlide(duplicatedSlide, text, pngFile, output_ppt);
										logger.debug("Inserted image: " + pngFile.getName());
									}
									// stop the while cycle
									break;
								}
							}

						}

						// stop searching in shapes - for convention, just one placeholder for slide
						break;

					}
				}
			}
			// check if slide was already duplicated
			if (!slideDuplicated) {
				duplicateSlide(slide, output_ppt);
				slideDuplicated = true;
			}

		}
		// TODO: change pptx file name
		FileOutputStream out = new FileOutputStream(documentExecutionPath + File.separator + "output.pptx");
		logger.debug("Final ppt file is created:  " + documentExecutionPath + File.separator + "output.pptx");
		output_ppt.write(out);
		out.close();

		ppt.close();
		output_ppt.close();
		inputstream.close();
		File outputFile = new File(documentExecutionPath + File.separator + "output.pptx");
		return outputFile;
	}

	private XSLFSlide duplicateSlide(XSLFSlide slide, XMLSlideShow output_ppt) {
		XSLFSlide newSlide = output_ppt.createSlide();
		XSLFSlideLayout src_sl = slide.getSlideLayout();
		XSLFSlideMaster src_sm = slide.getSlideMaster();

		XSLFSlideLayout new_sl = newSlide.getSlideLayout();
		XSLFSlideMaster new_sm = newSlide.getSlideMaster();

		// copy source layout to the new layout
		new_sl.importContent(src_sl);
		// copy source master to the new master
		new_sm.importContent(src_sm);
		newSlide.importContent(slide);
		return newSlide;
	}

	private void insertImageIntoSlide(XSLFSlide slide, String placeholder, File image, XMLSlideShow ppt) throws FileNotFoundException, IOException {
		List<XSLFShape> shapes = slide.getShapes();
		for (XSLFShape shape : shapes) {
			if (shape instanceof XSLFTextShape) {
				String text = ((XSLFTextShape) shape).getText();
				if (text.startsWith("<<") && text.endsWith(">>")) {
					// clean string to get just placeholder name
					text = text.replaceAll("<<", "");
					text = text.replaceAll(">>", "");
					if (text.equalsIgnoreCase(placeholder)) {

						// get shape anchor
						java.awt.geom.Rectangle2D anchor = shape.getAnchor();
						FileInputStream img = new FileInputStream(image);
						// insert image
						byte[] picture = IOUtils.toByteArray(img);

						// adding the image to the presentation
						XSLFPictureData pd = ppt.addPicture(picture, PictureData.PictureType.PNG);

						// creating a slide with given picture on it
						XSLFPictureShape pic = slide.createPicture(pd);

						pic.setAnchor(anchor);
						slide.removeShape(shape);
						img.close();
					}
				}
			}
		}
	}

	private Map<String, File> getMapOfFiles(File parentDocumentsFolder) {
		Map<String, File> mapOfFiles = new HashMap<String, File>();
		// get subfolders
		File[] subFolders = parentDocumentsFolder.listFiles();
		for (File subFolder : subFolders) {
			// search file (only one is supposed) inside folder
			if (subFolder.isDirectory()) {
				File[] files = subFolder.listFiles();
				for (File file : files) {
					mapOfFiles.put(subFolder.getName(), file);
				}
			}
		}
		return mapOfFiles;
	}

	private static File getDossierExecutionFolder() {
		logger.debug("IN");

		String resourcePath = DossierEngineConfig.getInstance().getEngineResourcePath();

		resourcePath += RESOURCE_DOSSIER_EXECUTION_FOLDER;

		File directory = new File(resourcePath);
		if (!directory.exists()) {
			directory.mkdir();
		}
		logger.debug("OUT");
		return directory;

	}

	@Override
	public String getEngineName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpServletRequest getServletRequest() {

		return request;
	}
}
