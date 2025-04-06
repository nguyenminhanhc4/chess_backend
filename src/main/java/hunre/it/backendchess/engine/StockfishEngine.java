package hunre.it.backendchess.engine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class StockfishEngine {
    private Process engineProcess;
    private Writer engineInput;
    private BufferedReader engineOutput;

    // Khởi chạy Stockfish process
    public void startEngine() throws Exception {
        // Đường dẫn tương đối đến file stockfish (đảm bảo rằng file này nằm trong thư mục dự án của bạn)
        ProcessBuilder pb = new ProcessBuilder("stockfish/stockfish");
        engineProcess = pb.start();
        engineInput = new OutputStreamWriter(engineProcess.getOutputStream());
        engineOutput = new BufferedReader(new InputStreamReader(engineProcess.getInputStream()));
    }

    // Gửi lệnh đến Stockfish
    public void sendCommand(String command) throws Exception {
        engineInput.write(command + "\n");
        engineInput.flush();
    }

    // Đọc output từ Stockfish
    public String readOutput() throws Exception {
        StringBuilder output = new StringBuilder();
        String line;
        // Đọc cho đến khi gặp dòng bắt đầu bằng "bestmove"
        while ((line = engineOutput.readLine()) != null) {
            output.append(line).append("\n");
            if (line.startsWith("bestmove")) {
                break;
            }
        }
        return output.toString();
    }

    // Dừng engine
    public void stopEngine() {
        if (engineProcess != null) {
            engineProcess.destroy();
        }
    }
}
