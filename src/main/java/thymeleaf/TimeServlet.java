package thymeleaf;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;

    @Override
    public void init() throws ServletException {
        engine = new TemplateEngine();

        JavaxServletWebApplication jswa =
                JavaxServletWebApplication.buildApplication(this.getServletContext());

        WebApplicationTemplateResolver
                resolver = new WebApplicationTemplateResolver(jswa);

        resolver.setPrefix("/WEB-INF/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");

        String param = req.getParameter("timezone");

        String lastTimezone = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("lastTimezone")) {
                    lastTimezone = cookie.getValue();
                }
            }
        }

        String timezone;
        if (param != null && !param.isEmpty()) {
            timezone = param.replace(" ", "+");
        } else if (lastTimezone != null && !lastTimezone.isEmpty()) {
            timezone = lastTimezone;
        } else {
            timezone = "GMT";
        }

        resp.addCookie(new Cookie("lastTimezone", timezone));
        Instant currentTime = Instant.now();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        TimeZone timeZone = TimeZone.getTimeZone(timezone);
        dateFormat.setTimeZone(timeZone);

        String formattedTime = dateFormat.format(Date.from(currentTime));

        Context simpleContext = new Context(req.getLocale(),
                Map.of("time", formattedTime));

        engine.process("time", simpleContext, resp.getWriter());
        resp.getWriter().close();
    }
}
