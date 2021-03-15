package pgdp.net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
//import java.nio.file.Files;
//import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PinguPlagWebServer implements Runnable {

	static int port = 80;
	private PinguTextCollection collection;
	private HtmlGenerator generator;
	private ServerSocket serverSocket;
	private boolean running;



	public PinguPlagWebServer() throws IOException {
		// TODO
		this.generator = new HtmlGenerator();
		this.collection = new PinguTextCollection();
		this.serverSocket = new ServerSocket(port);
		this.running = true;
	}

	public static void main(String[] args) throws IOException {
		PinguPlagWebServer pinguPlagWebServer = new PinguPlagWebServer();
		while (!Thread.currentThread().isInterrupted()) {
			Thread th = new Thread(pinguPlagWebServer);
			th.start();
			try {
				th.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public void run() {
		// TODO
		while (!Thread.currentThread().isInterrupted()) {
			Socket client = null;
			try {
				client = serverSocket.accept();
				System.out.println("Connection to: " + client.getInetAddress() + " established");
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
				List<String> lines = new ArrayList<>();
				String firstLine = in.readLine();
				String body = tryReadBody(in);
				try {
					out.write(this.handleRequest(firstLine, body).toString());
					out.flush();
				} catch (InvalidRequestException e) {
					System.out.println("Invalid request!");
					out.println(new HttpResponse(HttpStatus.NOT_FOUND, ""));
				}

			}  catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	HttpResponse handleRequest(String firstLine, String body) {
		if (firstLine == null) {
			return new HttpResponse(HttpStatus.BAD_REQUEST, "Invalid HttpRequest");
		}
		HttpRequest request = null;
		try {
			request = new HttpRequest(firstLine, body);
			if (request.getPath().equals("/")) {
				return handleStartPage(request);
			}
			else if (request.getPath().equals("/texts")) {
				return this.handleNewText(request);
			} else {
				return this.handleTextDetails(request);
			}
		} catch (InvalidRequestException e) {
			System.out.println("Invalid request!");
			if (request != null && request.getPath() == null) {
				return new HttpResponse(HttpStatus.NOT_FOUND, "NOT_FOUND");
			}
			return new HttpResponse(HttpStatus.BAD_REQUEST, generator.generateStartPage(collection.getAll()));
		}

	}

	PinguTextCollection getPinguTextCollection() {
		return collection;
	}

	HttpResponse handleStartPage(HttpRequest request) {
		if (!request.getMethod().equals(HttpMethod.GET)) {
			return new HttpResponse(HttpStatus.METHOD_NOT_ALLOWED, generator.generateStartPage(collection.getAll()));
		}
		return new HttpResponse(HttpStatus.OK, this.generator.generateStartPage(this.collection.getAll()));
		/*HttpStatus status;
		String body;
		switch (request.getMethod()){
			case GET -> {
				status = HttpStatus.OK;
				if(request.getPath().equals("/")){
					return new HttpResponse(status,generator.generateStartPage(this.collection.getAll()));
				}
				if(request.getPath().startsWith("/texts/")){
					long zahl = Integer.parseInt(request.getPath().split("/")[3]);
					Map<PinguText,Double> plag = this.collection.findPlaiarismFor(zahl);
					if(plag!=null){
						return new HttpResponse(status,generator.generateTextDetailsPage(collection.findById(zahl),plag));
					}
					else {
						return new HttpResponse(HttpStatus.BAD_REQUEST,"Invalid id!");
					}
				}
				break;
			}
			case POST -> {
				if(!Files.exists(Path.of(request.getPath()))){
					status = HttpStatus.NOT_FOUND;
				}
				else if(request.getParameters()==null){
					status=HttpStatus.BAD_REQUEST;
				}
				else {
					status = HttpStatus.OK;
				}
				break;
			}
			default -> throw new IllegalStateException("Unexpected value: " + request.getMethod());
		}

		return null; // TODO
		*/
	}

	HttpResponse handleTextDetails(HttpRequest request) {
		if (!request.getMethod().equals(HttpMethod.GET)) {
			return new HttpResponse(HttpStatus.METHOD_NOT_ALLOWED, generator.generateStartPage(collection.getAll()));
		}
		String[] splitted = request.getPath().split("/");
		long id = -1;
		if (splitted.length != 3) {
			return new HttpResponse(HttpStatus.BAD_REQUEST, generator.generateStartPage(collection.getAll()));
		}
		else {
			try {
				id = Long.parseLong(splitted[2]);
			} catch (NumberFormatException e) {
				return new HttpResponse(HttpStatus.BAD_REQUEST, generator.generateStartPage(collection.getAll()));
			}
		}
		PinguText toFind = collection.findById(id);
		Map<PinguText, Double> result = collection.findPlagiarismFor(id);
		if (result == null) {
			return new HttpResponse(HttpStatus.BAD_REQUEST, generator.generateStartPage(collection.getAll()));
		}

		return new HttpResponse(HttpStatus.OK, generator.generateTextDetailsPage(toFind, result));
	}

	HttpResponse handleNewText(HttpRequest request) {
		if (!request.getMethod().equals(HttpMethod.POST)) {
			return new HttpResponse(HttpStatus.METHOD_NOT_ALLOWED, generator.generateStartPage(collection.getAll()));
		}
		PinguText added = collection.add(request.getParameters().get("title"), request.getParameters().get("author"), request.getParameters().get("text"));
		HttpResponse response = new HttpResponse(HttpStatus.SEE_OTHER, null, "/texts/" + (collection.getAll().size() - 1));
		return response;

	}

	/**
	 * Tries to read a HTTP request body from the given {@link BufferedReader}.
	 * Returns null if no body was found. This method consumes all lines of the
	 * request, read the first line of the HTTP request before using this method.
	 */
	static String tryReadBody(BufferedReader br) throws IOException {
		String contentLengthPrefix = "Content-Length: ";
		int contentLength = -1;
		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.isEmpty()) {
				if (contentLength == -1)
					return null;
				char[] content = new char[contentLength];
				int read = br.read(content);
				if (read == -1)
					return null;
				if (read < content.length)
					content = Arrays.copyOf(content, read);
				return new String(content);
			}
			if (line.startsWith(contentLengthPrefix)) {
				try {
					contentLength = Integer.parseInt(line.substring(contentLengthPrefix.length()));
				} catch (@SuppressWarnings("unused") RuntimeException e) {
					// ignore and just continue
				}
			}
		}
		return null;
	}


}

