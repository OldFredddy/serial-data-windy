package org.chsbk;

import com.google.gson.Gson;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import static spark.Spark.*;

public class Main {

    private static Wind wind = new Wind();
    private static DeviceReader deviceReader;
    private static Timer timerRefreshData;
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        // Инициализация DeviceReader
        String portName = "COM3"; // Замените на ваш порт
        if (args.length > 0) {
            portName = args[0];
        }
        deviceReader = new DeviceReader(portName);

        // Периодическое чтение данных
        timerRefreshData = new Timer();
        timerRefreshData.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double windSpeed = deviceReader.getWindSpeed();
                String timestamp = LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);

                // Обновление объекта wind
                wind.setValue(String.valueOf(windSpeed));
                wind.setTimestamp(timestamp);

                // Вывод скорости ветра в консоль
                System.out.println("Скорость ветра: " + windSpeed + " м/с в " + timestamp);
            }
        }, 0, 500); // Каждые 500 мс

        // Запуск Spark сервера
        port(4567); // При необходимости измените порт
        get("/getwind", (request, response) -> {
            response.type("application/json");
            return gson.toJson(wind);
        });

        System.out.println("Сервер запущен на http://localhost:4567/getwind");

        // Добавление хука для корректного завершения работы
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (timerRefreshData != null) {
                timerRefreshData.cancel();
            }
            if (deviceReader != null) {
                deviceReader.close();
            }
            stop();
            System.out.println("Приложение остановлено.");
        }));
    }
}
