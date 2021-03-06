package org.opendaylight.iotdm.client.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by wenxshi on 11/12/15.
 */
public class Terminal {

    public static void main(String[] args) throws IOException {
        boolean flag = true;

        while (flag) {
            System.out.print(">>");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String commandStr = br.readLine().toLowerCase();
            if (commandStr.equals("exit"))
                return;
            Command command = new Command();
            command.parse(commandStr);
            command.execute();
            System.out.println(commandStr);
        }
    }
}
