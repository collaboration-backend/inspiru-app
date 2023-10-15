package com.stc.inspireu.utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.stc.inspireu.beans.MailMetadata;

@Component
public class EmailUtil {

    @Value("${ui.url}")
    private String uiUrl;

    @Value("${ui.resetPasswordPath}")
    private String resetPasswordPath;

    @Value("${spring.mail.username}")
    private String springMailUsername;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private FileAdapter fileAdapter;

    @Async("asyncExecutor")
    public void sendEmail(MailMetadata mailMetadata) throws MessagingException {

        Resource logoR = resourceLoader.getResource("classpath:static/images/logo.png");

        Resource welR = resourceLoader.getResource("classpath:static/images/welcome-bg.png");

        MimeMessage message = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
            StandardCharsets.UTF_8.name());

        Context context = new Context();

        context.setVariables(mailMetadata.getProps());

        String html = "";
        Map<String, File> attachments = new HashMap<>();
        if (mailMetadata.getTemplateString() != null) {
            html = mailMetadata.getTemplateString();

            while (html.contains("\"data:image")) {
                int startIndex = html.indexOf("\"data:image");
                int endIndex = startIndex + 11 + html.split("\"data:image")[1].indexOf("\"");
                String attachment = html.substring(startIndex + 1, endIndex);
                String cid = UUID.randomUUID().toString();
                byte[] decodedImg = DatatypeConverter.parseBase64Binary(attachment.split(",")[1]);
                File destinationFile;
                try {
                    String extension;
                    if (attachment.contains("data:image/svg"))
                        extension = "svg";
                    else if (attachment.contains("data:image/png"))
                        extension = "png";
                    else if (attachment.contains("data:image/jpg"))
                        extension = "jpg";
                    else extension = "jpeg";
                    destinationFile = new File(System.getProperty("java.io.tmpdir") + "/" + cid + "." + extension);
                    Files.write(destinationFile.toPath(), decodedImg);
                    attachments.put(cid, destinationFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                html = html.replace(attachment, "cid:" + cid);
            }
        } else {
            html = templateEngine.process("emails/" + mailMetadata.getTemplateFile(), context);
        }

        helper.setFrom(springMailUsername);

        if (mailMetadata.getTos() != null && mailMetadata.getTos().size() > 0) {
            String[] tos = mailMetadata.getTos().stream().toArray(String[]::new);
            helper.setTo(tos);
        } else {
            helper.setTo(mailMetadata.getTo());
        }

        helper.setText(html, true);
        if (Objects.nonNull(mailMetadata.getAttachments())) {
            mailMetadata.getAttachments().forEach(f -> {
                try {
                    String fileName = f;
                    if (f.contains("/"))
                        fileName = f.substring(f.lastIndexOf("/") + 1);
                    helper.addAttachment(fileName, fileAdapter.getFile(f, fileName));
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            });
        }
//        helper.addInline("myLogo", logoR);
        for (Map.Entry<String, File> entry : attachments.entrySet()) {
            helper.addInline(entry.getKey(), entry.getValue());
        }
        if (mailMetadata.getSubject().equals(null) || mailMetadata.getSubject().equals("")) {
            helper.setSubject("Subject ");
        } else {
            helper.setSubject(mailMetadata.getSubject());
        }

        javaMailSender.send(message);

    }

    public String getTemplateHtml(String hdr, String content, String footer, String link, String linkName, String language) {

        String first = "<html lang=\"" + language + "\" xmlns=\"http://www.w3.org/1999/xhtml\">\r\n" + "\r\n" + "<head>\r\n"
            + "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\r\n"
            + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
            + "    <title></title>\r\n" + "    <style type=\"text/css\">\r\n" + "        body {\r\n"
            + "            font-family: \"Open Sans\", \"Segoe UI\", Roboto, Oxygen, Ubuntu, \"Helvetica Neue\", sans-serif;\r\n"
            + "        }\r\n" + "\r\n" + "        body,\r\n" + "        table,\r\n" + "        td,\r\n"
            + "        a {\r\n" + "            -webkit-text-size-adjust: 100%;\r\n"
            + "            -ms-text-size-adjust: 100%;\r\n" + "        }\r\n" + "    </style>\r\n" + "</head>\r\n"
            + "\r\n" + "<body dir=\"" + (language.equals("en") ? "ltr" : "rtl") + "\" \r\n"
            + "    style=\"width:100% !important;margin-top:0 !important;margin-bottom:0 !important;margin-right:0 !important;margin-left:0 !important;padding-top:0 !important;padding-bottom:0 !important;padding-right:0 !important;padding-left:0 !important;-webkit-font-smoothing:antialiased;text-size-adjust:100%;background-color:rgb(255, 255, 255);-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%;height:100% !important;\">\r\n"
            + "    <div style=\"width: 600px; max-width: 600px; margin-left: auto; margin-right: auto;\">\r\n"
            + "        <table style=\"width: 600px; max-width: 600px; margin-left: auto; margin-right: auto;\">\r\n"
            + "            <tr>\r\n" + "                <td>\r\n"
            + "                    <div class=\"spacer\" style=\"width: 100%; height: 30px;\"></div>";

        String logo = "<div style=\"text-align: center;\">\r\n"
            + "                        <a href=\"#\">      <img alt=\"STC Logo\" src=\"cid:myLogo\">  </a>\r\n"
            + "                    </div>";

        String header = "<div class=\"spacer\" style=\"width: 100%; height: 30px;\"></div>\r\n" + "\r\n"
            + "                    <div\r\n"
            + "                        style=\"text-align: center; background: #4f008c url(cid:myWelcome) 100% no-repeat; border-radius: 4px; padding-top: 25px; padding-bottom: 40px;\">\r\n"
            + "                        <div class=\"banner-detail\">\r\n"
            + "                            <div class=\"sec-title default normal mb-8 font-28\">\r\n"
            + "                                <h3 style=\"color: #fff; text-align: center; font-size: 28px; margin-bottom: 8px;\">\r\n"
            + "                                    Welcome to STC InspireU</h3>\r\n"
            + "                            </div>\r\n" + "                        </div>\r\n"
            + "                    </div>\r\n" + "\r\n"
            + "                    <div class=\"spacer\" style=\"width: 100%; height: 30px;\"></div>";

        String last = "                </td>\r\n" + "            </tr>\r\n" + "        </table>\r\n" + "    </div>\r\n"
            + "</body>\r\n" + "\r\n" + "</html>";

        String html = "";

        if (link == null || link == "") {
            html = first + hdr + getBody(content) + footer + last;
        } else {
            html = first + hdr + getBody(content) + getButton(link, linkName) + footer + last;
        }
        html = html.replace("class=\"ql-direction-rtl\"", "style='direction:rtl'");
        return applyCSS(html);
    }

    public String getTemplateHtml(String hdr, String content, String footer, String link, String linkName) {

        String first = "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\r\n" + "\r\n" + "<head>\r\n"
            + "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\r\n"
            + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
            + "    <title></title>\r\n" + "    <style type=\"text/css\">\r\n" + "        body {\r\n"
            + "            font-family: \"Open Sans\", \"Segoe UI\", Roboto, Oxygen, Ubuntu, \"Helvetica Neue\", sans-serif;\r\n"
            + "        }\r\n" + "\r\n" + "        body,\r\n" + "        table,\r\n" + "        td,\r\n"
            + "        a {\r\n" + "            -webkit-text-size-adjust: 100%;\r\n"
            + "            -ms-text-size-adjust: 100%;\r\n" + "        }\r\n" + "    </style>\r\n" + "</head>\r\n"
            + "\r\n" + "<body \r\n"
            + "    style=\"width:100% !important;margin-top:0 !important;margin-bottom:0 !important;margin-right:0 !important;margin-left:0 !important;padding-top:0 !important;padding-bottom:0 !important;padding-right:0 !important;padding-left:0 !important;-webkit-font-smoothing:antialiased;text-size-adjust:100%;background-color:rgb(255, 255, 255);-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%;height:100% !important;\">\r\n"
            + "    <div style=\"width: 600px; max-width: 600px; margin-left: auto; margin-right: auto;\">\r\n"
            + "        <table style=\"width: 600px; max-width: 600px; margin-left: auto; margin-right: auto;\">\r\n"
            + "            <tr>\r\n" + "                <td>\r\n"
            + "                    <div class=\"spacer\" style=\"width: 100%; height: 30px;\"></div>";

        String logo = "<div style=\"text-align: center;\">\r\n"
            + "                        <a href=\"#\">      <img alt=\"STC Logo\" src=\"cid:myLogo\">  </a>\r\n"
            + "                    </div>";

        String header = "<div class=\"spacer\" style=\"width: 100%; height: 30px;\"></div>\r\n" + "\r\n"
            + "                    <div\r\n"
            + "                        style=\"text-align: center; background: #4f008c url(cid:myWelcome) 100% no-repeat; border-radius: 4px; padding-top: 25px; padding-bottom: 40px;\">\r\n"
            + "                        <div class=\"banner-detail\">\r\n"
            + "                            <div class=\"sec-title default normal mb-8 font-28\">\r\n"
            + "                                <h3 style=\"color: #fff; text-align: center; font-size: 28px; margin-bottom: 8px;\">\r\n"
            + "                                    Welcome to STC InspireU</h3>\r\n"
            + "                            </div>\r\n" + "                        </div>\r\n"
            + "                    </div>\r\n" + "\r\n"
            + "                    <div class=\"spacer\" style=\"width: 100%; height: 30px;\"></div>";

        String last = "                </td>\r\n" + "            </tr>\r\n" + "        </table>\r\n" + "    </div>\r\n"
            + "</body>\r\n" + "\r\n" + "</html>";

        String html = "";

        if (link == null || link == "") {
            html = first + hdr + getBody(content) + footer + last;
        } else {
            html = first + hdr + getBody(content) + getButton(link, linkName) + footer + last;
        }

        return applyCSS(html);
    }

    String getBody(String html) {

        String body = "<div style=\"color: #000;\">\r\n" + "                        <div>\r\n"
            + "                            " + html + "\r\n" + "                        </div>\r\n"
            + "                    </div>";
        return body;
    }

    String getButton(String link, String name) {

        String btn = "<div style=\"text-align: center;\">\r\n"
            + "                        <div class=\"spacer\" style=\"width: 100%; height: 20px;\"></div>\r\n"
            + "                        <a href=\"" + link
            + "\" style=\"width: auto; cursor: pointer; background-color: #FF375E; color: #fff; font-size: 14px; font-weight: normal; padding: 10px; border-radius: 4px; height: 48px; line-height: 48px; min-width: auto; padding-left: 2rem; padding-right: 2rem; border: 0; outline: 0;text-decoration: none;\">"
            + name + "</a>\r\n" + "                    </div>";

        return btn;
    }

    String getFooter(String html) {
        String footer = "<div class=\"spacer\" style=\"width: 100%; height: 50px;\"></div>\r\n" + "\r\n"
            + "                    <div style=\"background: #f3f3f1; padding: 30px 20px; border-radius: 4px;\">\r\n"
            + "                        " + html + "\r\n" + "                    </div>\r\n" + "\r\n"
            + "                    <div class=\"spacer\" style=\"width: 100%; height: 30px;\"></div>";
        return footer;
    }

    public String replaceEmailToken(String html, Map<String, String> tokenValues) {

        html = html.replace("<span class=\"ql-mention-denotation-char\">#</span>", "");

        if (tokenValues != null) {
            for (Map.Entry<String, String> entry : tokenValues.entrySet()) {
                html = html.replace(entry.getKey(), entry.getValue());
            }
        }

        return html;
    }

    public String centerDivWrapper(String c) {
        String r = "<div style=\"width:100%; text-align:center;\">\r\n"
            + "<div style=\"display:inline-block;*display:inline;*zoom:1;overflow:hidden;text-align:left;\">\r\n"
            + " " + c + "\r\n" + "</div>\r\n" + "</div>";
        return r;
    }

    public String evaluationSlotMail(String link,String phase){
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "   <head>\n" +
            "      <meta charset=\"utf-8\">\n" +
            "      <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
            "      <title></title>\n" +
            "   </head>\n" +
            "   <body>\n" +
            "      <p>\n" +
            "         <b>Dear Entrepreneur,</b><br/><br/>\n" +
            "\n" +
            "Congratulations on advancing to the "+(phase.toLowerCase())+" phase of the inspireU General program!<br/><br/>\n" +
            "\n" +
            "We are excited to invite you to pitch your startup in person to our internal panel of experts.<br/>\n" +
            "\n" +
            "Please use the link to reserve your slot: <a href='"+link+"'>Pitching reservation</a><br/><br/>\n" +
            "Kindly select your slot carefully, ensuring it fits comfortably with your schedule.  <br/>\n" +
            "\n" +
            "Here are some key details and suggestions to help you prepare for the pitching:<br/>\n" +
            "\n" +
            "      </p>\n" +
            "   \t\n" +
            "      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:collapse\">\n" +
            "         <tbody>\n" +
            "            <tr>\n" +
            "               <td width=\"173\" style=\"width:175.25pt;border:solid windowtext 1.0pt;background:#4f0091;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">Date &amp; Time</span></p>\n" +
            "               </td>\n" +
            "               <td width=\"146\" valign=\"top\" style=\"width:146.1pt;border:solid windowtext 1.0pt;border-left:none;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p align=\"center\" style=\"text-align:center\"><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;;color:#0d0d0d\">August 1<sup>st</sup></span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;;color:#1f497d\">\n" +
            "                     </span><span style=\"font-size:10.0pt;font-family:&quot;Cambria&quot;,serif;color:#0d0d0d\">&nbsp;</span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;;color:#0d0d0d\">– August 3<sup>rd</sup>.</span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\"></span>\n" +
            "                  </p>\n" +
            "               </td>\n" +
            "               <td width=\"151\" valign=\"top\" style=\"width:164.35pt;border:solid windowtext 1.0pt;border-left:none;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p align=\"center\" style=\"text-align:center\"><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;;color:#0d0d0d\">10:00 am – 04:00 pm</span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\"></span></p>\n" +
            "               </td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "               <td width=\"173\" style=\"width:175.25pt;border:solid windowtext 1.0pt;border-top:none;background:#4f0091;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">Location</span></p>\n" +
            "               </td>\n" +
            "               <td width=\"298\" colspan=\"2\" valign=\"top\" style=\"width:310.45pt;border-top:none;border-left:none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p align=\"center\" style=\"text-align:center\"><a href=\"https://goo.gl/maps/wcfmAjvQgcLwZFzj9\" target=\"_blank\" data-saferedirecturl=\"https://www.google.com/url?q=https://goo.gl/maps/wcfmAjvQgcLwZFzj9&amp;source=gmail&amp;ust=1690542979981000&amp;usg=AOvVaw2esMAbMkLSSuHxI7sPfItV\"><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;;color:#1f497d\">inspireU from stc building\n" +
            "                     </span></a><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;;color:#0d0d0d\">2nd\n" +
            "                     </span><span style=\"font-size:10.0pt;font-family:&quot;Cambria&quot;,serif;color:#0d0d0d\">&nbsp;</span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;;color:#0d0d0d\">floor or Webex</span>\n" +
            "                  </p>\n" +
            "               </td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "               <td width=\"173\" style=\"width:175.25pt;border:solid windowtext 1.0pt;border-top:none;background:#4f0091;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">Arrival Time</span></p>\n" +
            "               </td>\n" +
            "               <td width=\"298\" colspan=\"2\" valign=\"top\" style=\"width:310.45pt;border-top:none;border-left:none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p align=\"center\" style=\"text-align:center\"><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">Please plan to arrive at the venue\n" +
            "                     <b>20-30 minutes</b> ahead of your scheduled interview time.</span>\n" +
            "                  </p>\n" +
            "               </td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "               <td width=\"173\" style=\"width:175.25pt;border:solid windowtext 1.0pt;border-top:none;background:#4f0091;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">Cancellation</span></p>\n" +
            "               </td>\n" +
            "               <td width=\"298\" colspan=\"2\" valign=\"top\" style=\"width:310.45pt;border-top:none;border-left:none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p align=\"center\" style=\"text-align:center\"><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">Please schedule your appointment by this Saturday,\n" +
            "                     <b>July 29th, at 12 PM </b>KSA time, at the latest. If you miss your appointment or do not schedule one, we will assume that you are no longer interested in the program and your application will be withdrawn.</span>\n" +
            "                  </p>\n" +
            "               </td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "               <td width=\"173\" style=\"width:175.25pt;border:solid windowtext 1.0pt;border-top:none;background:#4f0091;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">Pitch timing</span></p>\n" +
            "               </td>\n" +
            "               <td width=\"298\" colspan=\"2\" valign=\"top\" style=\"width:310.45pt;border-top:none;border-left:none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p align=\"center\" style=\"text-align:center\"><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">Presentation:\n" +
            "                     <b>5 minutes</b></span>\n" +
            "                  </p>\n" +
            "                  <p align=\"center\" style=\"text-align:center\"><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">Questions:\n" +
            "                     <b>5 minutes</b></span>\n" +
            "                  </p>\n" +
            "                  <p align=\"center\" style=\"text-align:center\"><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">Note: please be prepared to deliver your pitch in 5 minutes or less. If you go over the time limit, you will be asked to stop</span></p>\n" +
            "               </td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "               <td width=\"173\" style=\"width:175.25pt;border:solid windowtext 1.0pt;border-top:none;background:#4f0091;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">Pitch-deck Requirement\n" +
            "                     </span>\n" +
            "                  </p>\n" +
            "               </td>\n" +
            "               <td width=\"298\" colspan=\"2\" valign=\"top\" style=\"width:310.45pt;border-top:none;border-left:none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family:Symbol\"><span>·<span style=\"font:7.0pt &quot;Times New Roman&quot;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
            "                     </span></span></span><span dir=\"LTR\"></span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">value proposition</span>\n" +
            "                  </p>\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family:Symbol\"><span>·<span style=\"font:7.0pt &quot;Times New Roman&quot;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
            "                     </span></span></span><span dir=\"LTR\"></span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">customer segmentation and targeted customers</span>\n" +
            "                  </p>\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family:Symbol\"><span>·<span style=\"font:7.0pt &quot;Times New Roman&quot;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
            "                     </span></span></span><span dir=\"LTR\"></span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">current stage of devolvement/ solution</span>\n" +
            "                  </p>\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family:Symbol\"><span>·<span style=\"font:7.0pt &quot;Times New Roman&quot;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
            "                     </span></span></span><span dir=\"LTR\"></span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">revenue model</span>\n" +
            "                  </p>\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family:Symbol\"><span>·<span style=\"font:7.0pt &quot;Times New Roman&quot;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
            "                     </span></span></span><span dir=\"LTR\"></span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">competitive advantage</span>\n" +
            "                  </p>\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family:Symbol\"><span>·<span style=\"font:7.0pt &quot;Times New Roman&quot;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
            "                     </span></span></span><span dir=\"LTR\"></span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">team members\n" +
            "                     </span>\n" +
            "                  </p>\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family:Symbol\"><span>·<span style=\"font:7.0pt &quot;Times New Roman&quot;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
            "                     </span></span></span><span dir=\"LTR\"></span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">market opportunity\n" +
            "                     </span>\n" +
            "                  </p>\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family:Symbol\"><span>·<span style=\"font:7.0pt &quot;Times New Roman&quot;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
            "                     </span></span></span><span dir=\"LTR\"></span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">financial tractions</span>\n" +
            "                  </p>\n" +
            "               </td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "               <td width=\"173\" style=\"width:175.25pt;border:solid windowtext 1.0pt;border-top:none;background:#4f0091;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\">Pitch-deck Format</span></p>\n" +
            "               </td>\n" +
            "               <td width=\"298\" colspan=\"2\" valign=\"top\" style=\"width:310.45pt;border-top:none;border-left:none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;padding:0in 5.4pt 0in 5.4pt\">\n" +
            "                  <p align=\"center\" style=\"text-align:center\"><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;;color:#1f1f1f;background:white\">Please bring your presentation on a USB drive in\n" +
            "                     <b>Microsoft PowerPoint</b> or <b>PDF format</b></span><span style=\"font-size:10.0pt;font-family: Segoe UI, Roboto;\"></span>\n" +
            "                  </p>\n" +
            "               </td>\n" +
            "            </tr>\n" +
            "         </tbody>\n" +
            "      </table>\n" +
            "<p>\n" +
            "         Please rehearse your presentation thoroughly so that you can deliver it confidently and effectively. This will help you make a lasting impression on the panel and increase your chances of being selected for the final bootcamp stage.<br/><br/>\n" +
            "We look forward to witnessing your captivating presentation and learning more about your startup.<br/><br/>\n" +
            "<span style=\"font-size: 12px;\">In case you have any inquiry please contact inspireu@stc.com.sa write down your startup name & your contact number</span><br/><br/>\n" +
            "<b>Best regards,</b><br/>\n" +
            "inspireU Team\n" +
            "      </p>"+
            "   </body>\n" +
            "</html>";
    }

    private String applyCSS(String html) {
        while (html.contains("class=")) {
            String[] htmlSplit = html.split("class=");
            int classStartIndex = html.indexOf("class=");
            int classEndIndex = html.indexOf("\"", classStartIndex + 7);
            int elementStartIndex = htmlSplit[0].lastIndexOf("<");
            int elementEndIndex = htmlSplit[1].indexOf(">");
            String element = html.substring(elementStartIndex, html.indexOf("class=") + 7 + elementEndIndex);
            String[] classNames = htmlSplit[1].substring(1, htmlSplit[1].indexOf("\"", 1)).split(" ");
            String styleAppliedElement = element;
            String elementStart = element.split(" ")[0];
            for (String className : classNames) {
                String style = "";
                switch (className) {
                    case "ql-align-center":
                        style = "text-align:center";
                        break;
                    case "ql-align-right":
                        style = "text-align: right";
                        break;
                    case "ql-align-justify":
                        style = "text-align: justify";
                        break;
                    case "ql-font-monospace":
                        style = "font-family: Monaco,Courier New,monospace!important";
                        break;
                    case "ql-font-serif":
                        style = "font-family: Georgia,Times New Roman,serif!important";
                        break;
                    case "ql-size-small":
                        style = "font-size: .75em";
                        break;
                    case "ql-size-large":
                        style = "font-size: 1.5em";
                        break;
                    case "ql-size-huge":
                        style = "font-size: 2.5em!important";
                        break;
                    case "ql-syntax":
                        style = "background-color: #23241f;color: #f8f8f2;overflow: visible;white-space: pre-wrap;margin-bottom: 5px;margin-top: 5px;padding: 5px 10px;border-radius: 3px";
                        break;
                    default:
                        break;
                }
                if (element.contains("style=\"") && !style.isEmpty()) {
                    styleAppliedElement = styleAppliedElement.replace("style=\"", "style=\"" + style + ";");
                } else if (!style.isEmpty())
                    styleAppliedElement = styleAppliedElement.replace(elementStart, elementStart + " style=\"" + style + "\"");
            }
            styleAppliedElement = styleAppliedElement.replace(html.substring(classStartIndex, classEndIndex + 1), "");
            html = html.replace(element, styleAppliedElement);

        }
        return html;
    }
}


class Test {

    public static void main(String[] args) {
        String html = "<div id='dv-1' class=\"dv-1 cls-2\" style=\"display:none\"></div>" +
            "<span id='span-1'/>" +
            "<img class=\"dv-1\"/>";
        while (html.contains("class=")) {
            String[] htmlSplit = html.split("class=");
            int classStartIndex = html.indexOf("class=");
            int classEndIndex = html.indexOf("\"", classStartIndex + 7);
            int elementStartIndex = htmlSplit[0].lastIndexOf("<");
            int elementEndIndex = htmlSplit[1].indexOf(">");
            String element = html.substring(elementStartIndex, html.indexOf("class=") + 7 + elementEndIndex);
            String[] classNames = htmlSplit[1].substring(1, htmlSplit[1].indexOf("\"", 1)).split(" ");
            String styleAppliedElement = element;
            String elementStart = element.split(" ")[0];
            for (String className : classNames) {
                String style = "";
                switch (className) {
                    case "dv-1":
                        style = "color:red";
                        break;
                    case "cls-2":
                        style = "border-radius:5px";
                        break;
                    default:
                        break;
                }
                if (element.contains("style=\"") && !style.isEmpty()) {
                    styleAppliedElement = styleAppliedElement.replace("style=\"", "style=\"" + style + ";");
                } else if (!style.isEmpty())
                    styleAppliedElement = styleAppliedElement.replace(elementStart, elementStart + " style=\"" + style + "\"");
            }
            styleAppliedElement = styleAppliedElement.replace(html.substring(classStartIndex, classEndIndex + 1), "");
            html = html.replace(element, styleAppliedElement);

        }
    }
}
