package org.rapidpm.event.gf;

import com.tinkerforge.*;

import java.io.IOException;
import java.text.DecimalFormat;

import static java.lang.System.exit;

/**
 * Created by svenruppert on 04.09.15.
 */
public class Main {
  private static final String host = "localhost";
  private static final int port = 4223;
  private static final int CALLBACK_PERIOD = 1_000;

  private static final IPConnection ipcon = new IPConnection();

  private static final BrickletSegmentDisplay4x7 sevenSegment = new BrickletSegmentDisplay4x7("iW3", ipcon);
  private static final BrickletPiezoSpeaker piezoSpeaker = new BrickletPiezoSpeaker("iN2", ipcon);

  private static final BrickletLCD20x4 lcd = new BrickletLCD20x4("jvX", ipcon);


  private static final BrickletPTC ptc = new BrickletPTC("i2J", ipcon);

  private static final byte[] DIGITS = {0x3f, 0x06, 0x5b, 0x4f,
      0x66, 0x6d, 0x7d, 0x07,
      0x7f, 0x6f, 0x77, 0x7c,
      0x39, 0x5e, 0x79, 0x71}; // 0~9,A,b,C,d,E,F

  private static final DecimalFormat myFormatter = new DecimalFormat("0000");

  public static void main(String[] args) {
    WaitForQ waitForQ = new WaitForQ();
    try {
      ipcon.connect(host, port);

      lcd.backlightOn();



      ptc.setTemperatureCallbackPeriod(CALLBACK_PERIOD);
      ptc.addTemperatureListener(temperature -> {
        final double celcius = temperature / 100.0;
        System.out.println("celcius = " + celcius);

        try {
          lcd.clearDisplay();
          lcd.writeLine((short) 0, (short) 0, "Temp: " + celcius);
          if (celcius >= -10) writeTo7Segment(celcius);
        } catch (TimeoutException
            | NotConnectedException e) {
          e.printStackTrace();
        }
      });

      waitForQ.addShutDownAction(() -> {
        try {
          ipcon.disconnect();
        } catch (NotConnectedException ignored) {
        }
      });
      waitForQ.addShutDownAction(()-> exit(0));
      waitForQ.waitForQ();

    } catch (IOException
        | AlreadyConnectedException
        | TimeoutException
        | NotConnectedException e) {
      e.printStackTrace();
      try {
        ipcon.disconnect();
      } catch (NotConnectedException ignored) {
      }
    } finally {

    }
  }

  private static void writeTo7Segment(double celcius) throws TimeoutException, NotConnectedException {
    final char[] chars = myFormatter.format(celcius).toCharArray();
    System.out.println(chars);
    short[] segments = {
        DIGITS[chars[0] - 48],
        DIGITS[chars[1] - 48],
        DIGITS[chars[2] - 48],
        DIGITS[chars[3] - 48]};
    sevenSegment.setSegments(segments, (short) 7, true);
  }





}
