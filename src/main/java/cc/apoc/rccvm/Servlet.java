package cc.apoc.rccvm;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public class Servlet extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger("rccvm.web");
	
	Config config;
	TaskQueue queue;
	
	InternalClient internal;
	
	public Servlet(Config config, TaskQueue queue) {
		this.config = config;
		this.queue = queue;
		
		internal = new InternalClient(config);
	}

	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		doAny(HttpMethod.GET, request, response);
	}
	
	protected void doPost(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		doAny(HttpMethod.POST, request, response);
	}

	private void doAny(HttpMethod method, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String url = request.getRequestURI();
		String postData = (method == HttpMethod.POST) ? readPostData(request) : null;
		
		response.setContentType("application/json");
		String content = "{}";
		try {
			
			logger.info("servlet request - " + url);
			
			if (url.contains("/backends"))
				content = internal.get("/backends");
			
			else if (url.startsWith("/execute") && config.bypass_task_queue)
				content = internal.post("/execute", postData);
			
			else if (url.startsWith("/task"))
				if (method == HttpMethod.GET)
					content = queue.getTask(url.substring("/task".length()+1)).toJson();
				else
					content = queue.createTask(postData).toJson();
			
			else
				throw new Exception("unhandled route");

		}
		catch (Exception e) {
			logger.error("servlet request error, " + e.toString());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			content = String.format("{\"error\": %s}", new Gson().toJson(e.getMessage()));
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(content);
	}
	
	private String readPostData(HttpServletRequest request) {
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) {}
		return jb.toString();
	}
}
