/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.documentcomposition.exporterUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

public class PdfCreator {

	Integer videoHeight;

	Integer videoWidth;

	float docHeight;

	float docWidth;

	static private Logger logger = Logger.getLogger(PdfCreator.class);

	public FileOutputStream createPdfFile(FileOutputStream fileOutputStream, Map<String, DocumentContainer> documentsMap, boolean defaultStyle)
			throws MalformedURLException, IOException, DocumentException {

		logger.debug("IN");

		Document document = new Document(PageSize.A4.rotate());
		Rectangle rect = document.getPageSize();
		docWidth = rect.getWidth();
		docHeight = rect.getHeight();

		logger.debug("document size width: " + docWidth + " height: " + docHeight);

		// PdfWriter writer=PdfWriter.getInstance(document,new
		// FileOutputStream("C:/comp/SpagoBIProva.pdf"));
		PdfWriter writer = PdfWriter.getInstance(document, fileOutputStream);
		document.open();

		int documentsNumber = documentsMap.keySet().size();
		int columnnsNumber = 2;

		if (defaultStyle == true) {
			logger.debug("use default style");
			int cellsCounter = 0;

			PdfPTable table = new PdfPTable(columnnsNumber);
			table.setWidthPercentage(100);

			for (Iterator iterator = documentsMap.keySet().iterator(); iterator.hasNext();) {
				String label = (String) iterator.next();
				DocumentContainer docContainer = documentsMap.get(label);
				byte[] content = docContainer.getContent();
				if (content != null) {
					Image img = null;
					try {
						img = Image.getInstance(content);
						table.addCell(img);
					} catch (Exception e) {
						logger.debug("Trying to evaluate response as a PDF file... ");
						table.addCell("");
						// try {
						// PdfReader reader = new PdfReader(content);
						// PdfImportedPage page = writer.getImportedPage(reader,
						// 1);
						// writer.addPage(page);
						// table.addCell("");
						// } catch (Exception x) {
						// logger.error("Error in inserting image for document "
						// + label, e);
						// logger.error("Error in inserting pdf file for document "
						// + label, x);
						// table.addCell("");
						// }
					}
				}
				cellsCounter++;
			}

			// if cell counter is not pair make it pair
			if (cellsCounter % 2 != 0) {
				table.addCell("");
			}
			document.add(table);

		} else { // ************* NO DEFAULT STYLE *****************
			logger.debug("No default style");

			// I want to calculate total height of scaled heights!!
			// int totalScaledHeight=calculateTotaleScaledHeights(documentsMap,
			// defaultStyle);

			// run on all documents
			for (Iterator iterator = documentsMap.keySet().iterator(); iterator.hasNext();) {
				String label = (String) iterator.next();
				logger.debug("document with label " + label);

				DocumentContainer docContainer = documentsMap.get(label);
				MetadataStyle style = docContainer.getStyle();

				// one table for each image, set at absolute position
				PdfPTable table = new PdfPTable(1);

				// width and height specified for the container by style
				// attribute
				int widthStyle = style.getWidth();
				int heightStyle = style.getHeight();
				logger.debug("style for document width: " + widthStyle + " height: " + heightStyle);

				// width and height for the table scaled to the document size
				int tableWidth = calculatePxSize(docWidth, widthStyle, videoWidth);
				int tableHeight = calculatePxSize(docHeight, heightStyle, videoHeight);

				logger.debug("table for document width: " + tableWidth + " height: " + tableHeight);

				// x and y position as specified for the container by the style
				// attribute
				int yStyle = style.getY();
				int xStyle = style.getX();
				// width and height scaled to the document size
				int xPos = (calculatePxPos(docWidth, xStyle, videoWidth));
				int yPos = (int) docHeight - (calculatePxPos(docHeight, yStyle, videoHeight));
				logger.debug("Table position at x: " + xPos + " y: " + yPos);

				// get the image
				byte[] content = docContainer.getContent();
				if (content != null) {
					Image img = null;
					try {
						img = Image.getInstance(content);
					} catch (Exception e) {
						logger.debug("Trying to evaluate response as a PDF file... ");
						try {
							PdfReader reader = new PdfReader(content);
							PdfContentByte cb = writer.getDirectContent();
							PdfImportedPage page = writer.getImportedPage(reader, 1);
							float[] tm = getTransformationMatrix(page, xPos, yPos, tableWidth, tableHeight);
							cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);
						} catch (Exception x) {
							logger.error("Error in inserting image for document " + label, e);
							logger.error("Error in inserting pdf file for document " + label, x);
						}
						continue;
					}

					// if it is a REPORT and has more than one page, too large,
					// you have to resize the image, but how to understand it?
					// if image size is more than double of the container size
					// cut the first part,otherwise scale it
					if (docContainer.getDocumentType().equals("REPORT")) {
						boolean cutImageWIdth = isToCutWidth(img, tableWidth);
						boolean cutImageHeight = isToCutHeight(img, tableWidth);

						if (cutImageWIdth == true || cutImageHeight == true) {
							logger.debug("Report will be cut to width " + tableWidth + " and height " + tableHeight);
							try {
								img = cutImage(content, cutImageHeight, cutImageWIdth, tableHeight, tableWidth, (int) img.getWidth(), (int) img.getHeight());
							} catch (Exception e) {
								logger.error("Error in image cut, cutt will be ignored and image will be drawn anyway ", e);
							}
						}
					}

					// this is percentage to resize
					// The image must be size within the cell
					int percToResize = percentageToResize((int) img.getWidth(), (int) img.getHeight(), tableWidth, tableHeight);
					logger.debug("image will be scaled of percentage " + percToResize);
					img.scalePercent(percToResize);

					PdfPCell cell = new PdfPCell(img);
					cell.setNoWrap(true);
					cell.setFixedHeight(tableHeight);
					cell.setBorderWidth(0);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setVerticalAlignment(Element.ALIGN_CENTER);
					table.addCell(cell);

					// table.setWidthPercentage(tableWidthPerc);
					table.setTotalWidth(tableWidth);
					table.setLockedWidth(true);
				} else {
					// TODO: setALT!
				}
				logger.debug("Add table");
				table.writeSelectedRows(0, -1, xPos, yPos, writer.getDirectContent());
				logger.debug("Document added");
			}

		}
		document.close();
		logger.debug("OUT");
		return fileOutputStream;
	}

	private float[] getTransformationMatrix(PdfImportedPage page, int xPos, int yPos, int tableWidth, int tableHeight) {
		float pageWidth = page.getWidth();
		float pageHeight = page.getHeight();
		float scaleX = tableWidth / pageWidth;
		float scaleY = tableHeight / pageHeight;
		float scale = Math.min(scaleX, scaleY);
		// float[] toReturn = {scale, 0f, 0f, scale, xPos, docHeight -
		// tableHeight};
		float dX = xPos;
		float dY = yPos - tableHeight;
		float[] toReturn = { scale, 0f, 0f, scale, dX, dY };
		return toReturn;
	}

	int chooseDefaultColumnsNumber(int documentsNumber) {

		// if(documentsNumber<=2)return documentsNumber;
		// else if(documentsNumber==3 || documentsNumber==4) return 2;
		// else return 3;
		return 2;

	}

	int calculatePercentage(float documentSize, int styleSize, int videoSize) {

		// this is x value to object in PDF
		int value = (styleSize * (int) documentSize) / videoSize;

		// get percentage of x within docyment
		int percentage = (value * 100) / (int) documentSize;
		return percentage;
	}

	int calculatePxSize(float documentSize, int styleSize, int videoSize) {

		// this is x value to object in PDF
		int value = (styleSize * (int) documentSize) / videoSize;
		return value;
		// // get percentage of x within docyment
		// int percentage=(value*100)/(int)documentSize;
		// return percentage;
	}

	int calculatePxPos(float documentSize, int styleSize, int videoSize) {

		// this is x value to object in PDF
		int value = (styleSize * (int) documentSize) / videoSize;

		return value;
	}

	public Integer getVideoHeight() {
		return videoHeight;
	}

	public void setVideoHeight(Integer videoHeight) {
		this.videoHeight = videoHeight;
	}

	public Integer getVideoWidth() {
		return videoWidth;
	}

	public void setVideoWidth(Integer videoWidth) {
		this.videoWidth = videoWidth;
	}

	/**
	 * called only if not default style
	 * 
	 * @param documentsMap
	 */
	int calculateTotalHeight(Map<String, DocumentContainer> documentsMap, boolean defaultStyle) {
		logger.debug("IN");
		int totalHeight = 0;
		if (defaultStyle == false) {
			// total height is the maximum top+height revealed!
			for (Iterator iterator = documentsMap.keySet().iterator(); iterator.hasNext();) {
				String label = (String) iterator.next();
				DocumentContainer docContainer = documentsMap.get(label);
				MetadataStyle style = docContainer.getStyle();
				int verticalLimit = style.getHeight() + style.getY();
				if (verticalLimit > totalHeight) {
					totalHeight = verticalLimit;
				}

			}
		}
		logger.debug("OUT");
		return totalHeight;
	}

	int calculateTotalWidth(Map<String, DocumentContainer> documentsMap, boolean defaultStyle) {
		logger.debug("IN");
		int totalWidth = 0;
		if (defaultStyle == false) {
			// total height is the maximum top+height revealed!
			for (Iterator iterator = documentsMap.keySet().iterator(); iterator.hasNext();) {
				String label = (String) iterator.next();
				DocumentContainer docContainer = documentsMap.get(label);
				MetadataStyle style = docContainer.getStyle();
				int horizontalLimit = style.getWidth() + style.getX();
				if (horizontalLimit > totalWidth) {
					totalWidth = horizontalLimit;
				}

			}
		}
		logger.debug("OUT");
		return totalWidth;
	}

	int calculateTotaleScaledHeights(Map<String, DocumentContainer> documentsMap, boolean defaultStyle) {
		logger.debug("IN");
		int totalHeight = 0;
		if (defaultStyle == false) {
			// total height is the maximum top+height revealed!
			for (Iterator iterator = documentsMap.keySet().iterator(); iterator.hasNext();) {
				String label = (String) iterator.next();
				DocumentContainer docContainer = documentsMap.get(label);
				MetadataStyle style = docContainer.getStyle();
				int height = style.getHeight() + style.getY();
				int verticalLimit = calculatePxSize(docHeight, height, videoHeight);
				if (verticalLimit > totalHeight) {
					totalHeight = verticalLimit;
				}

			}
		}
		logger.debug("OUT");
		return totalHeight;
	}

	int percentageToResize(int imgWidth, int imgHeight, int tableWidth, int tableHeight) {
		logger.debug("IN");
		int perc = 100;
		int percReductionWidth = 100;
		if (imgWidth > tableWidth) {
			percReductionWidth = (tableWidth * 100) / imgWidth;
		}
		int percReductionHeight = 100;
		if (imgHeight > tableHeight) {
			percReductionHeight = (tableHeight * 100) / imgHeight;
		}

		perc = percReductionHeight < percReductionWidth ? percReductionHeight : percReductionWidth;
		logger.debug("OUT");
		return perc;
	}

	// cutImage(content, cutImageHeight, cutImageWIdth, tableHeight,
	// tableWidth);

	Image cutImage(byte[] bytes, boolean cutImageHeight, boolean cutImageWidth, int tableHeight, int tableWidth, int imgWidth, int imgHeight)
			throws IOException, BadElementException {
		logger.debug("IN");

		BufferedImage image = null; // Read from a file
		BufferedImage region = null;

		int pxWidthToCut = (cutImageWidth == true) ? tableWidth : imgWidth;
		int pxHeightToCut = (cutImageHeight == true) ? tableHeight : imgHeight;

		InputStream inputStream = new ByteArrayInputStream(bytes);

		image = ImageIO.read(inputStream); // Read from an input stream
		try {
			region = image.getSubimage(0, 0, pxWidthToCut, pxHeightToCut);
		} catch (Exception e) {
			e.printStackTrace();
		}
		byte[] newBytes = getBytes(region);
		Image cutImg = Image.getInstance(newBytes);
		// ImageIO.write(region,"PNG",new File("C:/nuovaImmagine222.PNG"));
		logger.debug("OUT");

		return cutImg;
	}

	private byte[] getBytes(BufferedImage image) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "PNG", baos);
		return baos.toByteArray();
	}

	boolean isToCutWidth(Image img, int tableWidth) {
		int imgWidth = (int) img.getWidth();
		if (imgWidth > (4 * tableWidth))
			return true;
		else
			return false;
	}

	boolean isToCutHeight(Image img, int tableHeight) {
		int imgHeight = (int) img.getHeight();
		if (imgHeight > (4 * tableHeight))
			return true;
		else
			return false;
	}

}
