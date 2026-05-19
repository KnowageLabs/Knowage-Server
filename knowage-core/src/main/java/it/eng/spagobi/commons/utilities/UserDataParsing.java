package it.eng.spagobi.commons.utilities;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bo.UserBO;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDataParsing {

    public static List<UserBO> parseWithOpenCSV(InputStream is) throws Exception {

        List<UserBO> users = new ArrayList<>();

        IRoleDAO roleDao = DAOFactory.getRoleDAO();
        List<Role> allRolesFromDb = roleDao.loadAllRoles();
        Map<String, Integer> rolesCache = new HashMap<>();
        if (allRolesFromDb != null) {
            for (Role r : allRolesFromDb) {
                if (r.getName() != null) {
                    rolesCache.put(r.getName(), r.getId()); }
            }
        }

        ISbiAttributeDAO attributeDao = DAOFactory.getSbiAttributeDAO();
        List<SbiAttribute> allAttributeFromDB = attributeDao.loadSbiAttributes();
        HashMap<String,String> attributeMap = new HashMap<>();

        if (allAttributeFromDB != null) {
            for (SbiAttribute a : allAttributeFromDB) {
                if (a.getDescription() != null) {
                    attributeMap.put(a.getAttributeName(), ""); }
            }
        }


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                    .build();

            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                if (nextLine.length >= 2) {
                    UserBO user = new UserBO();
                    user.setUserId(nextLine[0].trim());
                    user.setPassword(nextLine[1].trim());
                    user.setFullName(nextLine[2].trim());
                    List<Integer> roles = new ArrayList<>();

                    for (int i= 3; i < nextLine.length; i++) {
                        if(nextLine[i].trim().contains(":")){

                            String[] parts= nextLine[i].trim().split(":",2);
                            String label  = parts[0].trim();
                            String value  = parts[1].trim();

                            attributeMap.replace(label, value);

                        } else {
                            Integer roleId = rolesCache.get(nextLine[i].trim());
                            if (roleId != null) {
                                roles.add(roleId);
                            } else {
                                System.err.println("Ruolo non trovato nel sistema: " + nextLine[i].trim()); }
                        }
                    }
                    HashMap<Integer,HashMap<String,String>> attributesMap = new HashMap<>();
                     int counter = 1;

                    for (SbiAttribute a : allAttributeFromDB) {
                        HashMap<String,String> attributeMapApp = new HashMap<>();

                        String label  = a.getAttributeName();
                        String value  = attributeMap.get(label);
                        Integer id =    a.getAttributeId();
                        attributeMapApp.put(label, value);
                        attributesMap.put(id, attributeMapApp);

                    }

                    user.setSbiExtUserRoleses(roles);
                    user.setSbiUserAttributeses(attributesMap);
                    users.add(user);
                }
            }
        } catch(Exception e) {
            // gestione errore
        }
        return users;
    }

    public static List<UserBO> parseWithApachePOI(InputStream is) throws Exception {

        List<UserBO> users = new ArrayList<>();

        IRoleDAO roleDao = DAOFactory.getRoleDAO();
        List<Role> allRolesFromDb = roleDao.loadAllRoles();
        Map<String, Integer> rolesCache = new HashMap<>();
        if (allRolesFromDb != null) {
            for (Role r : allRolesFromDb) {
                if (r.getName() != null) {
                    rolesCache.put(r.getName(), r.getId()); }
            }
        }

        ISbiAttributeDAO attributeDao = DAOFactory.getSbiAttributeDAO();
        List<SbiAttribute> allAttributeFromDB = attributeDao.loadSbiAttributes();
        HashMap<String,String> attributeMap = new HashMap<>();

        if (allAttributeFromDB != null) {
            for (SbiAttribute a : allAttributeFromDB) {
                if (a.getDescription() != null) {
                    attributeMap.put(a.getAttributeName(), ""); }
            }
        }


        // Utilizzo di un blocco try-with-resources per garantire la chiusura del Workbook
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                 if (row == null || row.getLastCellNum() < 3) {
                     continue;
                 }

                UserBO user = new UserBO();

                 user.setUserId(getCellValueAsString(row.getCell(0)));
                 user.setPassword(getCellValueAsString(row.getCell(1)));
                 user.setFullName(getCellValueAsString(row.getCell(2)));

                 List<Integer> roles = new ArrayList<>();
                  int lastCellNum = row.getLastCellNum();
                  for (int i = 3; i < lastCellNum; i++) {
                      Cell cell = row.getCell(i);
                      if (cell == null) {
                          continue;
                      }

                      String cellValue = getCellValueAsString(cell);
                      if (cellValue.isEmpty()) {
                          continue;
                      }

                      if (cellValue.contains(":")) {
                          String[] parts = cellValue.split(":", 2);
                          String label = parts[0].trim();
                          String value = parts[1].trim();

                          if (attributeMap.containsKey(label)) {
                              attributeMap.put(label, value);
                          }
                      } else {
                          Integer roleId = rolesCache.get(cellValue);
                          if (roleId != null) {
                              roles.add(roleId);
                          } else {
                              System.err.println("Ruolo non trovato nel sistema: " + cellValue);
                          }
                      }
                  }

                HashMap<Integer, HashMap<String, String>> attributesMap = new HashMap<>();
                if (allAttributeFromDB != null) {
                    for (SbiAttribute a : allAttributeFromDB) {
                        HashMap<String, String> attributeMapApp = new HashMap<>();
                        String label = a.getAttributeName();
                        String value = attributeMap.get(label);
                        Integer id = a.getAttributeId();
                        attributeMapApp.put(label, value);
                        attributesMap.put(id, attributeMapApp);
                    }
                }

                user.setSbiExtUserRoleses(roles);
                user.setSbiUserAttributeses(attributesMap);
                users.add(user);
            }
        } catch(Exception e) {
            // gestione errore
        }
        return users;
    }

    private static String getCellValueAsString(Cell cell) {
     if (cell == null) return "";
     DataFormatter formatter = new DataFormatter();
     return formatter.formatCellValue(cell).trim();
    }
}
