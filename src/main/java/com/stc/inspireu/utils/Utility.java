package com.stc.inspireu.utils;

import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.models.KeyValue;
import com.stc.inspireu.repositories.KeyValueRepository;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class Utility {


	@Autowired
	private KeyValueRepository keyValueRepository;

	public static String LEGACY_FORMAT = "EEE MMM dd hh:mm:ss zzz yyyy";

	private static final SimpleDateFormat legacyFormatter = new SimpleDateFormat(LEGACY_FORMAT);

	public String getSessionId() {
		UUID uuid = UUID.randomUUID();
		String uuidAsString = uuid.toString();
		return uuidAsString;
	}

    public static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            // As of https://en.wikipedia.org/wiki/X-Forwarded-For
            // The general format of the field is: X-Forwarded-For: client, proxy1, proxy2 ...
            // we only want the client
            return new StringTokenizer(xForwardedForHeader, ",").nextToken().trim();
        }
    }

	public String getUsername() {
		String id = RandomStringUtils.random(15, true, true);
		return id;
	}

	public String getAlhpaNumeric() {
		return getAlhpaNumeric(20);
	}

	public String getAlhpaNumeric(int in) {
		String id = RandomStringUtils.random(in, true, true);
		return id;
	}

	public String qrAlphaNumeric() {
		String id = RandomStringUtils.randomAlphanumeric(12);
		return id;
	}


	public Date addDays(int days) {
		Date now = new Date();
		long ltime = now.getTime() + days * 24 * 60 * 60 * 1000;
		Date next = new Date(ltime);
		return next;
	}

	public int getMonthFromDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int month = cal.get(Calendar.MONTH);
		return month + 1;
	}

	public int getDayFromDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int day = cal.get(Calendar.DATE);
		return day;
	}

	public int getYearFromDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int day = cal.get(Calendar.YEAR);
		return day;
	}

	public String getMonth(Integer month) {
		String[] monthNames = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
				"October", "November", "December" };
		return monthNames[month - 1];
	}

	public Date atStartOfDay(Date date) {
		LocalDateTime localDateTime = dateToLocalDateTime(date);
		LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
		return localDateTimeToDate(startOfDay);
	}

	public Date atEndOfDay(Date date) {
		LocalDateTime localDateTime = dateToLocalDateTime(date);
		LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
		return localDateTimeToDate(endOfDay);
	}

	public Map<String, Date> atStartAndEndOfDayWithTz(Integer day, Integer month, Integer year, String timezone) {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");

		String str = (month < 10 ? "0" + month : month) + "/" + (day < 10 ? "0" + day : day) + "/" + year;

		LocalDate localDate = LocalDate.parse(str, dtf);

		ZonedDateTime startOfDay = localDate.atStartOfDay(ZoneId.of(timezone));

		ZonedDateTime endOfDay = startOfDay.with(LocalTime.of(23, 59, 59));

		Map<String, Date> startAndEnd = new HashedMap<String, Date>();

		startAndEnd.put("start", Date.from(startOfDay.toInstant()));
		startAndEnd.put("end", Date.from(endOfDay.toInstant()));

		return startAndEnd;
	}

	private LocalDateTime dateToLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	private Date localDateTimeToDate(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	public Date atStartOfMonth(Date date, String timeZone) {

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		calendar.setTime(date);
		calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));

		return calendar.getTime();

	}

	public Date convertToDate(Integer year, Integer month, Integer day, String timeZone) {

		try {
			String str = (month < 10 ? "0" + month : month) + "/" + (day < 10 ? "0" + day : day) + "/" + year;

			Date date = new SimpleDateFormat("MM/dd/yyyy").parse(str);
			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
			calendar.setTime(date);
			// calendar.set(Calendar.DATE,
			// calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

			return calendar.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;

	}

	public Date atEndOfMonth(Date date, String timeZone) {

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		calendar.setTime(date);
		calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

		return calendar.getTime();
	}

	public String getFullURL(HttpServletRequest request) {
		StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
		String queryString = request.getQueryString();

		if (queryString == null) {
			return requestURL.toString();
		} else {
			return requestURL.append('?').append(queryString).toString();
		}
	}

	public boolean isUrlMatch(String url, String... antPatterns) {
		boolean isMatch = false;
		for (String pattern : antPatterns) {
			AntPathMatcher antPathMatcher = new AntPathMatcher();
			boolean isIndexFound = antPathMatcher.match(pattern, url);
			if (isIndexFound) {
				isMatch = true;
				break;
			}
		}
		return isMatch;
	}

	public int getNumberOfDaysInaMonth(Integer month, Integer year) {

		if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
			return 31;
		} else if (month == 4 || month == 6 || month == 9 || month == 11) {
			return 30;
		} else if (month == 2) {
			if ((year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0))) {
				return 29;
			} else {
				return 28;
			}

		} else {
			return 0;
		}
	}

	public Map<String, String> createExcelFile(Map<String, Object[]> data, String fileFormat) {
//Blank workbook
		XSSFWorkbook workbook = new XSSFWorkbook();
		// Create a blank sheet
		XSSFSheet sheet = workbook.createSheet("Attendance");
		// Iterate over data and write to sheet

		Set<String> keyset = data.keySet();
		int rownum = 0;
		for (String key : keyset) {
			Row row = sheet.createRow(rownum++);
			Object[] objArr = data.get(key);
			int cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				sheet.setColumnWidth(cellnum, 9500); // Set column width, you'll probably want to tweak the second int
				CellStyle style = workbook.createCellStyle(); // Create new style
				style.setWrapText(true); // Set wordwrap

				if (obj instanceof String) {
					style.setAlignment(HorizontalAlignment.CENTER);
					cell.setCellValue((String) obj);

				} else if (obj instanceof Integer) {
					cell.setCellValue((Integer) obj);
				}
				cell.setCellStyle(style); // Apply style to cell
			}
		}
		try {
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File("startup." + fileFormat));
			workbook.write(out);
			out.close();
			byte[] fileContent = FileUtils.readFileToByteArray(new File("startup." + fileFormat));
			Map<String, String> result = new HashMap<>();
			result.put("fileFormat", fileFormat);
			result.put("base64", Base64.getEncoder().encodeToString(fileContent));

			File fileToDelete = new File("startup." + fileFormat);
			boolean success = fileToDelete.delete();

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			Map<String, String> result = new HashMap<>();
			result.put("Error", "excel exporting failed");
			return result;
		}
	}

	public Map<String, String> createExcelFileWithMultipleSheet(List<Map<String, Object[]>> dataList,
			String fileFormat) {
//Blank workbook
		XSSFWorkbook workbook = new XSSFWorkbook();
		int j = 0;
		for (Map<String, Object[]> data : dataList) {
			// Create a blank sheet
			XSSFSheet sheet = workbook.createSheet("Attendance" + j++);
			// Iterate over data and write to sheet

			Set<String> keyset = data.keySet();
			int rownum = 0;
			for (String key : keyset) {
				Row row = sheet.createRow(rownum++);
				Object[] objArr = data.get(key);
				int cellnum = 0;
				for (Object obj : objArr) {
					Cell cell = row.createCell(cellnum++);
					sheet.setColumnWidth(cellnum, 9500); // Set column width, you'll probably want to tweak the second
															// int
					CellStyle style = workbook.createCellStyle(); // Create new style
					style.setWrapText(true); // Set wordwrap

					if (obj instanceof String) {
						style.setAlignment(HorizontalAlignment.CENTER);
						cell.setCellValue((String) obj);

					} else if (obj instanceof Integer) {
						cell.setCellValue((Integer) obj);
					}
					cell.setCellStyle(style); // Apply style to cell
				}
			}
		}
		try {
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File("startupMngt." + fileFormat));
			workbook.write(out);
			out.close();
			byte[] fileContent = FileUtils.readFileToByteArray(new File("startupMngt." + fileFormat));
			Map<String, String> result = new HashMap<>();
			result.put("fileFormat", fileFormat);
			result.put("base64", Base64.getEncoder().encodeToString(fileContent));

			File fileToDelete = new File("startupMngt." + fileFormat);
			boolean success = fileToDelete.delete();

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			Map<String, String> result = new HashMap<>();
			result.put("Error", "excel exporting failed");
			return result;
		}
	}

	public Object[] getDateByMonth(Integer month, Integer year) {
		Object[] date = new Object[] { "Dates" };
		ArrayList<Object> newObj = new ArrayList<Object>(Arrays.asList(date));
		try {
			String startDate = "1/" + month.toString() + "/" + year.toString();
			Date sDate = new SimpleDateFormat("dd/MM/yyyy").parse(startDate);
			int noDays = getNumberOfDaysInaMonth(month, year);
			Calendar cal = Calendar.getInstance();
			cal.setTime(sDate);
			for (int i = 0; i < noDays; i++) {
				sDate = cal.getTime();
				DateFormat dateFormat = new SimpleDateFormat("EEE dd-MM-YYYY");
				String strDate = dateFormat.format(sDate);
				newObj.add(strDate);
				cal.add(Calendar.DAY_OF_MONTH, 1);

			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return newObj.toArray();
	}

	public String toLegacyUTCString(Date date) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		return date.toString();
	}

	private String role1 = null;
	private String role2 = null;
	private String role3 = null;
	private String role4 = null;
	private ArrayList<String> rolev;

	public void saveToXML(String xml) {
		Document dom;
		Element e = null;

		// instance of a DocumentBuilderFactory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			// use factory to get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			// create instance of DOM
			dom = db.newDocument();

			// create the root element
			Element rootEle = dom.createElement("roles");

			// create data elements and place them under root
			e = dom.createElement("role1");
			e.appendChild(dom.createTextNode(role1));
			rootEle.appendChild(e);

			e = dom.createElement("role2");
			e.appendChild(dom.createTextNode(role2));
			rootEle.appendChild(e);

			e = dom.createElement("role3");
			e.appendChild(dom.createTextNode(role3));
			rootEle.appendChild(e);

			e = dom.createElement("role4");
			e.appendChild(dom.createTextNode(role4));
			rootEle.appendChild(e);

			dom.appendChild(rootEle);

			try {
				Transformer tr = TransformerFactory.newInstance().newTransformer();
				tr.setOutputProperty(OutputKeys.INDENT, "yes");
				tr.setOutputProperty(OutputKeys.METHOD, "xml");
				tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
				tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

				// send DOM to file
				tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(xml)));

			} catch (TransformerException te) {
				System.out.println(te.getMessage());
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		} catch (ParserConfigurationException pce) {
			System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
		}
	}

	public int getOtp() {
		Random r = new Random();
		int l = 123456;
		int h = 987654;
		int result = r.nextInt(h - l) + l;
		return result;
	}

	public Map<String, Object> checkFile(MultipartFile... files) {
		Map<String, Object> map = new HashMap<String, Object>();

		String fileTypes = "";
		long fileSize = 0, fs = 0;

		KeyValue x = keyValueRepository.findByKeyName(Constant.FILES_ALLOWDED.toString());

		if (x != null) {
			fileTypes = x.getValueName();
		}

		KeyValue y = keyValueRepository.findByKeyName(Constant.FILE_SIZE.toString());

		if (y != null) {
			fileSize = Long.parseLong(y.getValueName());
			fs = Long.parseLong(y.getValueName());
		}

		fileSize = fileSize * 1048576;

		map.put("isAllow", true);
		map.put("error", null);

		if (files != null) {
			for (MultipartFile file : files) {

				if (file != null) {
					long size = file.getSize();

					if (size > fileSize) {
						map.put("isAllow", false);
						map.put("error", "file size exceed " + fs + "MB");
						break;
					}

					String extension = FilenameUtils.getExtension(file.getOriginalFilename());

					if (fileTypes.toLowerCase().contains(extension.toLowerCase())) {
					} else {
						map.put("isAllow", false);
						map.put("error", "file not supported, only support " + String.join(",", fileTypes));
						break;
					}

					String contentType = file.getContentType();
					contentType = contentType == null ? contentType : "";

					if (fileTypes.toLowerCase().contains(contentType.toLowerCase())) {
					} else {
						map.put("isAllow", false);
						map.put("error", "file not supported, only support " + String.join(",", fileTypes));
						break;
					}
				}

			}
		}

		return map;
	}

	public Map<String, Date> atStartAndEndOfMonthWithTz(Integer month, Integer year, String timezone) {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");

		String str = (month < 10 ? "0" + month : month) + "/01/" + year;

		LocalDate localDate = LocalDate.parse(str, dtf);

		ZonedDateTime startOfDay = localDate.atStartOfDay(ZoneId.of(timezone));

		LocalDate lmd = localDate.withDayOfMonth(localDate.getMonth().length(localDate.isLeapYear()));

		ZonedDateTime sed = lmd.atStartOfDay(ZoneId.of(timezone));

		ZonedDateTime endOfDay = sed.with(LocalTime.of(23, 59, 59));

		Map<String, Date> startAndEnd = new HashedMap<String, Date>();

		startAndEnd.put("start", Date.from(startOfDay.toInstant()));
		startAndEnd.put("end", Date.from(endOfDay.toInstant()));

		return startAndEnd;
	}

	public Map<String, Date> atStartAndEndOfDayByMilli(Long dateInMilli, String timezone) {

		Date input = new Date(dateInMilli);

		LocalDate localDate = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		ZonedDateTime startOfDay = localDate.atStartOfDay(ZoneId.of("UTC"));

		ZonedDateTime endOfDay = startOfDay.with(LocalTime.of(23, 59, 59));

		Map<String, Date> startAndEnd = new HashedMap<String, Date>();

		startAndEnd.put("start", Date.from(startOfDay.toInstant()));
		startAndEnd.put("end", Date.from(endOfDay.toInstant()));

		return startAndEnd;

	}

}
