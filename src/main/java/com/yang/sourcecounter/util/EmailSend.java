package com.yang.sourcecounter.util;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Properties;

/**
 * Created by Administrator on 2018/10/11.
 */
public class EmailSend {

    private static EmailSend emailSend;

    private EmailSend() {
    }

    public synchronized static EmailSend getEmailSend() {
        if (EmailSend.emailSend == null) {
            EmailSend.emailSend = new EmailSend();
        }
        return EmailSend.emailSend;
    }

    public void sendReportEmail(String reportPath, Properties properties) throws Exception {

        String senderAccount = properties.getProperty("senderAccount");
        String senderPassword = properties.getProperty("senderPassword");
        String host = properties.getProperty("host");


        //1、连接邮件服务器的参数配置
        Properties props = new Properties();
        //设置用户的认证方式
        props.setProperty("mail.smtp.auth", "true");
        //设置传输协议
        props.setProperty("mail.transport.protocol", "smtp");
        //设置发件人的SMTP服务器地址
        props.setProperty("mail.smtp.host", host);
        //2、创建定义整个应用程序所需的环境信息的 Session 对象
        Session session = Session.getInstance(props);
        //设置调试信息在控制台打印出来
        session.setDebug(true);
        //3、创建邮件的实例对象
        Message msg = createMimeMessage(session, reportPath, properties, true);
        //4、根据session对象获取邮件传输对象Transport
        Transport transport = session.getTransport();
        //设置发件人的账户名和密码
        transport.connect(senderAccount, senderPassword);
        //发送邮件，并发送到所有收件人地址，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(msg, msg.getAllRecipients());
        //5、关闭邮件连接
        transport.close();
    }


    private Message createMimeMessage(Session session, String reportPath, Properties projectProperties, Boolean myself) throws Exception {
        //1.创建一封邮件的实例对象
        MimeMessage msg = new MimeMessage(session);
        //2.设置发件人地址
        msg.setFrom(new InternetAddress(projectProperties.getProperty("senderAddress")));
        /**
         * 3.设置收件人地址（可以增加多个收件人、抄送、密送），即下面这一行代码书写多行
         * MimeMessage.RecipientType.TO:发送
         * MimeMessage.RecipientType.CC：抄送
         * MimeMessage.RecipientType.BCC：密送
         */
        if (myself) {
            msg.addRecipients(MimeMessage.RecipientType.CC, projectProperties.getProperty("senderAddress"));
        } else {
            msg.addRecipients(MimeMessage.RecipientType.TO, getRecipientAddresses(projectProperties));
        }

        //4.设置邮件主题
        String subject = CommonsDataFormat.SDF_YMDHMS.format(new Date()) + "日 " + projectProperties.getProperty("subject");
        msg.setSubject(subject, "UTF-8");

        MimeBodyPart text = new MimeBodyPart();
        String content = CommonsDataFormat.SDF_YMDHMS.format(new Date()) + "日 " + projectProperties.getProperty("subject");
        text.setContent(content, "text/html;charset=UTF-8");//下面是设置邮件正文

        // 9. 创建附件"节点"
        MimeBodyPart attachment = new MimeBodyPart();
        // 读取本地文件
        DataHandler dh2 = new DataHandler(new FileDataSource(reportPath));
        // 将附件数据添加到"节点"
        attachment.setDataHandler(dh2);
        String name = dh2.getName();
        // 设置附件的文件名（需要编码）
        attachment.setFileName(MimeUtility.encodeWord(dh2.getName()));

        // 10. 设置（文本+图片）和 附件 的关系（合成一个大的混合"节点" / Multipart ）
        MimeMultipart mm = new MimeMultipart();
        mm.addBodyPart(text);
        mm.addBodyPart(attachment);     // 如果有多个附件，可以创建多个多次添加
        mm.setSubType("mixed");         // 混合关系

        // 11. 设置整个邮件的关系（将最终的混合"节点"作为邮件的内容添加到邮件对象）
        msg.setContent(mm);
        //设置邮件的发送时间,默认立即发送
        msg.setSentDate(new Date());

        return msg;
    }

    private Address[] getRecipientAddresses(Properties projectProperties) throws Exception {
        InternetAddress[] targetAddress;
        String recipientAddresses = projectProperties.getProperty("recipientAddresses");
        if (recipientAddresses != null && !"".equals(recipientAddresses)) {
            String[] addressStrs = recipientAddresses.split(",");
            if (addressStrs.length < 1) {
                throw new RuntimeException("无法获取收件人地址列表");
            }
            targetAddress = new InternetAddress[addressStrs.length];
            for (int i = 0; i < addressStrs.length; i++) {
                targetAddress[i] = new InternetAddress(addressStrs[i]);
            }
        } else {
            throw new RuntimeException("无法获取收件人地址列表");
        }
        return targetAddress;
    }
}
