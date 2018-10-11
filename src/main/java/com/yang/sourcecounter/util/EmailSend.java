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

        // 连接邮件服务器的参数配置
        Properties props = new Properties();
        // 设置用户的认证方式
        props.setProperty("mail.smtp.auth", "true");
        // 设置传输协议
        props.setProperty("mail.transport.protocol", "smtp");
        // 设置发件人的SMTP服务器地址
        props.setProperty("mail.smtp.host", host);
        // 创建定义整个应用程序所需的环境信息的 Session 对象
        Session session = Session.getInstance(props);
        // 设置调试信息在控制台打印出来
        session.setDebug(true);
        // 创建邮件的实例对象
        Message msg = createMimeMessage(session, reportPath, properties);
        // 根据session对象获取邮件传输对象Transport
        Transport transport = session.getTransport();
        // 设置发件人的账户名和密码
        transport.connect(senderAccount, senderPassword);
        // 发送邮件
        transport.sendMessage(msg, msg.getAllRecipients());
        // 关闭邮件连接
        transport.close();
    }


    private Message createMimeMessage(Session session, String reportPath, Properties projectProperties) throws Exception {

        MimeMessage mimeMessage = new MimeMessage(session);

        // 设置发件人地址
        mimeMessage.setFrom(new InternetAddress(projectProperties.getProperty("senderAddress")));

        // 设置收件人地址
        mimeMessage.addRecipients(MimeMessage.RecipientType.TO, getRecipientAddresses(projectProperties));

        // 设置邮件主题
        String subject = CommonsDataFormat.SDF_YMD_CHN.format(new Date()) + projectProperties.getProperty("subject");
        mimeMessage.setSubject(subject, "UTF-8");

        MimeMultipart mimeMultipart = new MimeMultipart();

        // 设置邮件正文
        MimeBodyPart text = new MimeBodyPart();
        String content = projectProperties.getProperty("emailContent");
        text.setContent(content, "text/html;charset=UTF-8");

        // 创建附件"节点"
        MimeBodyPart attachment = new MimeBodyPart();
        // 读取本地文件
        DataHandler dataHandler = new DataHandler(new FileDataSource(reportPath));
        // 将附件数据添加到"节点"
        attachment.setDataHandler(dataHandler);

        // 设置附件的文件名（需要编码）
        attachment.setFileName(MimeUtility.encodeText("代码统计.xls"));

        mimeMultipart.addBodyPart(text);
        mimeMultipart.addBodyPart(attachment);

        // 混合关系
        mimeMultipart.setSubType("mixed");

        mimeMessage.setContent(mimeMultipart);

        // 设置邮件的发送时间
        mimeMessage.setSentDate(new Date());

        return mimeMessage;
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
