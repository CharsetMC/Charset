package pl.asie.charset.lib.scheduler;

public final class ScheduledEvent {
	private final Runnable runnable;
	private boolean executed;

	ScheduledEvent(Runnable runnable) {
		this.runnable = runnable;
		this.executed = false;
	}

	public boolean hasExecuted() {
		return executed;
	}

	boolean run() {
		if (!executed) {
			runnable.run();
			executed = true;
			return true;
		} else {
			throw new RuntimeException("ScheduledEvent tried to execute twice!");
		}
	}
}
