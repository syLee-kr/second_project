package com.example.camping.campingService;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

//flask 서버 실행 로직(자동실행) 
@Service
public class FlaskService {
	private Process flaskProcess;

    public void startFlaskServer() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "src/main/resources/flask_api/start_flask.bat");
            processBuilder.redirectErrorStream(true);
            flaskProcess = processBuilder.start();

            // 콘솔 로그 출력 (실행 상태 모니터링 가능)
            BufferedReader reader = new BufferedReader(new InputStreamReader(flaskProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Flask] " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopFlaskServer() {
        if (flaskProcess != null) {
            flaskProcess.destroy(); // Flask 프로세스 종료
        }
    }
    
    //배포 후 적용 코드 
//    private static final String FLASK_SCRIPT_PATH = "src/main/resources/static/start_flask.bat";
//
//    public void startFlaskServer() {
//        try {
//            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", FLASK_SCRIPT_PATH);
//            processBuilder.start();
//            System.out.println("Flask 서버가 시작되었습니다.");
//        } catch (IOException e) {
//            System.err.println("Flask 서버 실행 중 오류 발생: " + e.getMessage());
//        }
//    }
}

