package com.trong.server;

import com.sun.net.httpserver.HttpServer;
import com.trong.server.Controller.ConclusionHistoryController;
import com.trong.server.Controller.FraudLogController;
import com.trong.server.Controller.ProcessingSummaryController;
import com.trong.server.Controller.UserController;
import com.trong.server.Controller.VideoController;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServerApp {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("✅ Server đang chạy tại http://localhost:8080");

        UserController userController = new UserController();
        VideoController videoController = new VideoController();
        ProcessingSummaryController historyController = new ProcessingSummaryController();
        FraudLogController fraudLogController = new FraudLogController();
        ConclusionHistoryController conclusionController = new ConclusionHistoryController();
        server.createContext("/streamVideo/", videoController::handleStreamVideo);
        server.createContext("/login", userController::handleLogin);
        server.createContext("/processVideo", videoController::handleProcessVideo);
        server.createContext("/saveFraudLogs", videoController::handleSaveResults);
        server.createContext("/getVideoHistory", historyController::handleGetHistory);
        server.createContext("/getFraudLogs", fraudLogController::handleGetFraudLogs);
        server.createContext("/getProcessingLogDetails", historyController::handleGetProcessingLogDetails);
        server.createContext("/editFraudLog", fraudLogController::handleEditFraudLog);
        server.createContext("/saveConclusionHistory", conclusionController::handleSaveConclusionHistory);
        server.createContext("/editFraudLogBatch", fraudLogController::handleEditFraudLogBatch);
        server.createContext("/saveConclusionHistoryBatch", conclusionController::handleSaveConclusionHistoryBatch);
        server.createContext("/updateProcessingLogStatus", historyController::handleUpdateProcessingLogStatus);

        server.setExecutor(null);
        server.start();
    }
}

