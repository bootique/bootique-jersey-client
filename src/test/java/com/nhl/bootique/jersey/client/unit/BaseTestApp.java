package com.nhl.bootique.jersey.client.unit;

import com.nhl.bootique.BQRuntime;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.command.CommandOutcome;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.log.DefaultBootLogger;

/**
 * A base app class for BOM integration tests.
 */
public abstract class BaseTestApp {

	private InMemoryPrintStream stdout;
	private InMemoryPrintStream stderr;

	public BaseTestApp() {
		this.stdout = new InMemoryPrintStream(System.out);
		this.stderr = new InMemoryPrintStream(System.err);
	}

	public CommandOutcome run(String... args) {

		Bootique bootique = Bootique.app(args).bootLogger(createBootLogger());
		configure(bootique);

		BQRuntime runtime = bootique.runtime();
		try {
			return runtime.getRunner().run();
		} finally {
			runtime.shutdown();
		}
	}

	protected abstract void configure(Bootique bootique);

	protected BootLogger createBootLogger() {
		return new DefaultBootLogger(true, stdout, stderr);
	}

	public String getStdout() {
		return stdout.toString();
	}

	public String getStderr() {
		return stderr.toString();
	}
}
