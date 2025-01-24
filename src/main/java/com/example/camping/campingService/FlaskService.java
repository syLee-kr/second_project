package com.example.camping.campingService;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class FlaskService {
    private Process flaskProcess;

    // Spring Boot 시작 시 자동으로 Flask 서버 실행
    @PostConstruct
    public void init() {
        try {
            if (!isFlaskServerRunning()) {
                startFlaskServer();
                System.out.println("Flask 서버가 자동으로 실행되었습니다.");
            } else {
                System.out.println("Flask 서버가 이미 실행 중입니다.");
            }
        } catch (Exception e) {
            System.err.println("Flask 서버 실행 중 오류 발생: " + e.getMessage());
        }
    }

    // Flask 서버 실행 메서드
    public void startFlaskServer() {
        int retries = 3;  // 최대 재시도 횟수
        while (retries > 0) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "src/main/resources/flask_api/start_flask.bat");
                processBuilder.redirectErrorStream(true);
                flaskProcess = processBuilder.start();

                // Flask 서버 로그 모니터링
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(flaskProcess.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("[Flask] " + line);
                        }
                    } catch (IOException e) {
                        System.err.println("Flask 로그 읽기 실패: " + e.getMessage());
                    }
                }).start();

                System.out.println("Flask 서버가 실행 중인지 확인 중...");

                Thread.sleep(5000);  // 5초 대기 후 상태 확인

                if (isFlaskServerRunning()) {
                    System.out.println("Flask 서버가 성공적으로 시작되었습니다.");
                    break;
                } else {
                    System.err.println("Flask 서버가 정상적으로 시작되지 않았습니다. 재시도 중...");
                }

            } catch (IOException | InterruptedException e) {
                System.err.println("Flask 서버 실행 오류: " + e.getMessage());
            }
            retries--;
        }

        if (retries == 0) {
            System.err.println("Flask 서버 실행에 실패했습니다. 수동으로 실행해 주세요.");
        }
    }

    // Flask 서버가 실행 중인지 확인하는 메서드
    public boolean isFlaskServerRunning() {
        try {
            Process checkProcess = new ProcessBuilder("cmd.exe", "/c", "tasklist | findstr python").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()));
            return reader.lines().anyMatch(line -> line.contains("python"));
        } catch (IOException e) {
            System.err.println("Flask 서버 상태 확인 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    // Flask 서버 종료 메서드
    public void stopFlaskServer() {
        if (flaskProcess != null && flaskProcess.isAlive()) {
            flaskProcess.destroy();
            System.out.println("Flask 서버가 종료되었습니다.");
        }
    }
}
