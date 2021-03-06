package org.roda.wui.servlets;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandler extends HttpServlet {

  private static final long serialVersionUID = -8243921066429750410L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    // Obtain the original exception
    final Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    final String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
    final Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    final String uri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

    // Log the exception
    LOGGER.error("[{}] {} : {}", statusCode, uri, message, throwable);

    String errorPage = String.format("error_%1$s.html", statusCode);

    // Forward to the friendly error page
    RequestDispatcher rd = request.getRequestDispatcher(errorPage);
    rd.forward(request, response);
  }
}
