package org.chsbk;

import com.fazecast.jSerialComm.SerialPort;

public class DeviceReader {

    private SerialPort comPort;

    public DeviceReader(String portName) {
        comPort = SerialPort.getCommPort(portName);
        comPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.ODD_PARITY);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 100, 0);

        if (!comPort.openPort()) {
            System.out.println("Не удалось открыть порт " + portName);
        } else {
            System.out.println("Порт " + portName + " успешно открыт.");
        }
    }

    public double getWindSpeed() {
        return sendCommand((byte) 0xC1);
    }

    private double sendCommand(byte command) {
        try {
            comPort.writeBytes(new byte[]{command}, 1);

            byte[] readBuffer = new byte[4];
            int numRead = comPort.readBytes(readBuffer, readBuffer.length);

            if (numRead == 4 && readBuffer[0] == 0x41) {
                int integerPart = readBuffer[2] & 0xFF;
                int fractionalPart = readBuffer[3] & 0xFF;
                return integerPart + fractionalPart / 100.0;
            } else {
                System.out.println("Неверный ответ от прибора.");
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void close() {
        if (comPort != null && comPort.isOpen()) {
            comPort.closePort();
            System.out.println("Порт закрыт.");
        }
    }
}
