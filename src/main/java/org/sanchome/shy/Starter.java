package org.sanchome.shy;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.sanchome.shy.engine.ApplicationClient;
import org.sanchome.shy.engine.ApplicationServer;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;

/**
 * This main SHY Starter class.
 * @author christophe@andral.fr
 *
 */
public class Starter {

	/**
	 * @param args '-server' start SHY in server mode, client mode otherwise.
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("java.hone          :"+System.getProperty("java.home"));
		System.out.println("java.class.path    :"+System.getProperty("java.class.path"));
		System.out.println("sun.boot.class.path:"+System.getProperty("sun.boot.class.path"));
		
		boolean clientRuntime = true;
		boolean wrappedMode   = false;
		boolean clientWrapped = false;
		boolean serverWrapped = false;
		
		wrappedMode = true;
		clientWrapped = true;
		
		if (args!=null && args.length > 1) {
			if ("-serverOnly".equals(args[0])) {
				clientRuntime = false;
			}
			else if ("-clientWrapper".equals(args[0])) {
				wrappedMode = true;
				clientWrapped = true;
			}
			else if ("-clientWrapper".equals(args[0])) {
				wrappedMode = true;
				serverWrapped = true;
			}
		}
		
		if (wrappedMode) {
			System.out.println("Wrapped mode");
			if (serverWrapped) {
				System.out.println("Wrapped server");
				ApplicationServer server = new ApplicationServer();
				server.start(JmeContext.Type.Headless);
			}
			if (clientWrapped) {
				System.out.println("Wrapped client");
				AppSettings settings = new AppSettings(false);
				settings.setTitle("SHY - Sheep hate you");
				settings.setSettingsDialogImage("textures/splash-screen.png");
				//settings.
				ApplicationClient app = new ApplicationClient();
				app.setSettings(settings);
				app.start();
			}
		} else {
			System.out.println("Non yet wrapped mode");
			Runtime runtime = Runtime.getRuntime();
		
			System.out.println("Starting server");
			Process serverProcess = runtime.exec(new String[] {
				"java",
				"-server",
				Starter.class.getName(),
				"-serverWrapper"
			});
			//serverProcess.setOut(System.out);
			//serverProcess.setErr(System.err);
		
		
			if (clientRuntime) {
				System.out.println("Starting client");
				Process clientProcess = runtime.exec(new String[] {
					"java",
					Starter.class.getName(),
					"-clientWrapper"
				});
				//clientProcess.setOut(System.out);
				//clientProcess.setErr(System.err);
				clientProcess.waitFor();
				
				InputStreamReader isr = new InputStreamReader (clientProcess.getInputStream());
				BufferedReader br = new BufferedReader (isr);
				String s = null;
				do {
					s = br.readLine ();
					if (s == null) break;
					System.out.println ("[out] " + s);
				} while (s!=null);

				clientProcess.getInputStream().close ();
				
				isr = new InputStreamReader (clientProcess.getErrorStream());
				br = new BufferedReader (isr);
				s = null;
				do {
					s = br.readLine ();
					if (s == null) break;
					System.out.println ("[err] " + s);
				} while (s!=null);

				clientProcess.getErrorStream().close ();
			}
		
			serverProcess.waitFor();
		}
	}
}
