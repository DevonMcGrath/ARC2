package ca.sqrlab.arc.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ProcessResult {
	
	private Process process;
	
	private String stdout;
	
	private String stderr;
	
	public ProcessResult() {}
	
	public ProcessResult(String cmd) {
		try {
			setProcess(Runtime.getRuntime().exec(cmd));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ProcessResult(Process process) {
		setProcess(process);
	}
	
	public void readStreams() {
		
		// No process
		if (process == null) {
			return;
		}
		
		// Read the streams
		try {
			
			// Read the buffers
			InputStream[] streams = {process.getInputStream(),
					process.getErrorStream()};
			String[] outs = {"", ""};
			int idx = 0;
			for (InputStream is : streams) {
				if (is == null) {
					continue;
				}
				byte[] buffer = new byte[1024];
				int length = 0;
				while ((length = is.read(buffer)) > 0) {
					buffer = Arrays.copyOf(buffer, length);
					outs[idx] += new String(buffer);
		        }
				idx ++;
			}
			
			this.stdout = outs[0];
			this.stderr = outs[1];
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setProcess(Process process) {
		this.process = process;
		this.stdout = null;
		this.stderr = null;
	}
	
	public Process getProcess() {
		return process;
	}
	
	public String getSTDOUT() {
		return stdout;
	}
	
	public String getSTDERR() {
		return stderr;
	}
}
