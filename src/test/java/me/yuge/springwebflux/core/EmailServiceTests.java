package me.yuge.springwebflux.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import me.yuge.springwebflux.core.service.EmailService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailServiceTests {
    @Autowired
    EmailService emailService;
    @Autowired
    Configuration configuration;

    @Ignore
    @Test
    public void testSendSimpleMessage() {
        emailService.sendSimpleMessage("", "welcome", "Test welcome");
    }

    @Ignore
    @Test
    public void testSendHtmlMessage() throws IOException, TemplateException {
        Map<String, Object> root = new HashMap<>();
        root.put("user", "foo");

        Template template = configuration.getTemplate("email/welcome.ftlh");
        Writer writer = new StringWriter();
        template.process(root, writer);

        emailService.sendHtmlMessage("", "welcome", writer.toString());
    }

    @Ignore
    @Test
    public void testFreeMarker() throws IOException, TemplateException {
        Map<String, Object> root = new HashMap<>();
        root.put("user", "Foo");

        Template template = configuration.getTemplate("email/welcome.ftlh");
        Writer writer = Files.newBufferedWriter(Paths.get("welcome.html"), StandardCharsets.UTF_8);
        template.process(root, writer);
    }
}
