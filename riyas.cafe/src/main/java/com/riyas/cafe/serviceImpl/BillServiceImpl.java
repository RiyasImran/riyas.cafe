package com.riyas.cafe.serviceImpl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.riyas.cafe.JWT.JwtFilter;
import com.riyas.cafe.constants.CafeConstants;
import com.riyas.cafe.dao.BillDao;
import com.riyas.cafe.models.Bill;
import com.riyas.cafe.service.BillService;
import com.riyas.cafe.utils.CafeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImpl implements BillService {
    @Autowired
    JwtFilter jwtFilter;
    @Autowired
    BillDao billDao;
    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("inside generate report");
        try{
            String fileName;
            if (validateRequestMap(requestMap)){
                if (requestMap.containsKey("isGenerate") && !(Boolean) requestMap.get("isGenerate")){
                    fileName = (String) requestMap.get("uuid");
                } else {
                    fileName =CafeUtils.getUUID();
                    requestMap.put("uuid",fileName);
                    insertBill(requestMap);
                }
                String data = getData(requestMap);
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(CafeConstants.STORE_LOCATION+"/"+fileName+".pdf"));
                document.open();
                SetRectangleInPdf(document);

                Paragraph chunk = new Paragraph("Cafe Management System", getFont("Header"));
                chunk.setAlignment(Element.ALIGN_CENTER);
                document.add(chunk);

                Paragraph paragraph = new Paragraph(data + "\n \n", getFont("Data"));
                document.add(paragraph);

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addTableHeader(table);
                JSONArray jsonArray = new JSONArray((String)requestMap.get("productDetails"));
                for (int i=0; i< jsonArray.length(); i++){
                    addRows(table, CafeUtils.getJson(jsonArray.getString(1)));
                }
                document.add(table);

                Paragraph footer = new Paragraph("Total :"+ requestMap.get("total")+"\n"+
                        "Thank you please visit again", getFont("Data"));
                document.add(footer);

                document.close();
                return new ResponseEntity<>(" "+fileName, HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("Required Data not found", HttpStatus.BAD_REQUEST);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
        try{
            List<Bill> list;
            if (jwtFilter.isAdmin()){
                list = billDao.getAllBills();
            } else
                list = billDao.getBillByUserName(jwtFilter.getCurrentUser());
            return new ResponseEntity<>(list, HttpStatus.OK);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        try{
            log.info("inside getPdf: requestMap {}", requestMap);
            byte[] bytes = new byte[0];
            if (!requestMap.containsKey("uuid") && validateRequestMap(requestMap))
                return new ResponseEntity<>(bytes, HttpStatus.BAD_REQUEST);
            String filePath = CafeConstants.STORE_LOCATION+"/"+(String) requestMap.get("uuid")+".pdf";
            if (CafeUtils.isFileExist(filePath)){
                bytes = getByteArray(filePath);
                return new ResponseEntity<>(bytes, HttpStatus.OK);
            } else {
                requestMap.put("isGenerate", false);
                generateReport(requestMap);
                bytes = getByteArray(filePath);
                return new ResponseEntity<>(bytes, HttpStatus.OK);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try{
                Optional optional = billDao.findById(id);
                if (!optional.isEmpty()){
                    billDao.deleteById(id);
                    return CafeUtils.getResponseEntity("Bill deleted successfully", HttpStatus.OK);
                } else
                    return CafeUtils.getResponseEntity("Bill id doesn't exist", HttpStatus.OK);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private byte[] getByteArray(String filePath) throws Exception{
        File file = new File(filePath);
        InputStream targetStream = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(targetStream);
        return bytes;
    }

    private void addRows(PdfPTable table, Map<String, Object> json) {
        log.info("inside addrows");
        table.addCell((String) json.get("name"));
        table.addCell((String) json.get("category"));
        table.addCell((String) json.get("quantity"));
        table.addCell(Double.toString((Double) json.get("price")));
        table.addCell(Double.toString((Double) json.get("total")));


    }

    private void addTableHeader(PdfPTable table) {
        log.info("inside addTableHeader");
        Stream.of("Name","Category", "Quantity", "Price", "SubTotal")
                .forEach(columnTitle->{
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setBackgroundColor(BaseColor.YELLOW);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private Font getFont(String header) {
        log.info("inside getFont");
        Font font;
        switch (header){
            case "Header":
                font = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18, BaseColor.BLACK);
                font.setStyle(Font.BOLD);
                return font;
            case "Data":
                font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
                font.setStyle(Font.BOLD);
                return font;
            default:
                return  new Font();
        }
    }

    private void SetRectangleInPdf(Document document) throws DocumentException {
        Rectangle rect = new Rectangle(577,825,18,15);
        rect.enableBorderSide(1);
        rect.enableBorderSide(2);
        rect.enableBorderSide(4);
        rect.enableBorderSide(8);
        rect.setBorderColor(BaseColor.BLACK);
        rect.setBorderWidth(1);
        document.add(rect);
    }

    private String getData(Map<String, Object> requestMap) {
        return "Name" + requestMap.get("name")  + "\n" +
                "Contact Number" + requestMap.get("contactNumber") + "\n" +
                "Email" + requestMap.get("email") + "\n" +
                "Payment Method" + requestMap.get("paymentMethod");
    }

    private void insertBill(Map<String, Object> requestMap) {
        try{
            Bill bill = new Bill();
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setContactNumber((String) requestMap.get("contactNumber"));
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
            bill.setProductDetails((String) requestMap.get("productDetails"));
            bill.setTotal(Integer.parseInt((String) requestMap.get("total")));
            bill.setCreatedBy(jwtFilter.getCurrentUser());
            billDao.save(bill);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean validateRequestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey("name") &&
                requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("productDetails") &&
                requestMap.containsKey("total");
    }
}
