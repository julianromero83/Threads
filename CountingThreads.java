// Note that we're implementing Runnable here, NOT subclassing Thread.
// While the latter would work, it locks us into a particular inheritance
// hierarchy and is therefore much less flexible than implementing Runnable
class Counter implements Runnable {
	
	int startNumber;
	
	// This constructor allows us to use the same class for odds or evens
	// We just pass a different starting number when we create the instance.
	public Counter(int startAt) {
		startNumber = startAt;
	}
	
	public void run() {
		
		try {
			// Our counting loop. The point of Thread.sleep() is just to pause
			// the counting threads long enough to make the numbers appear on 
			// the screen slowly, rather than all at once.
			for(int i = startNumber; i <= 100; i+= 2) {
				System.out.println(i);
				Thread.sleep(100);
			}
		}
		catch(InterruptedException e) 
		{
			// Have to catch this exception whenever
			// we call sleep, just in case the thread is interrupted before
			// it wakes up.
		}
	}
}

// This is our heartbeat class. It's created in a way that there are two
// methods for stopping it. An "old school" method of watching a boolean
// flag that is triggered by the caller, and a way to do it using interrupt()
class Heartbeat implements Runnable {
	
	// We have to mark this variable as volatile to make sure that changes 
	// to it are seen by all threads, even if they are running in relatively
	// tight loops. 
	// (See http://stackoverflow.com/questions/106591/do-you-ever-use-the-volatile-keyword-in-java)
	volatile boolean keepRunning;

	public Heartbeat() {
		keepRunning = true;
	}
	
	// We can call this method from our main thread to stop the heartbeat loop.
	public void stopBeating() {
		keepRunning = false;	
	}
	
	public void run() {
		
		while(keepRunning) {
			
			System.out.println("Running...");
			
			try {
				Thread.sleep(1000);
			}
			catch(InterruptedException e) {
				// We can also call the interrupt() method from the main thread to 
				// trigger this exception, which will also end the loop.
				keepRunning = false;
			}
		}
	}
	
}

class CountManager {
	
	public static void main(String[] args) {
		
		// Note how we're storing these as Runnables instead of Counters. 
		// This is another example of "programming to an interface, not an implementation."
		Runnable odds = new Counter(1);
		Runnable evens = new Counter(0);
		
		// In this case though, we can't do that because we want the option of calling 
		// the stopBeating() method. We could cast the instance back to Heartbeat later,
		// but there's no real advantage to either approach.
		Heartbeat heart = new Heartbeat();
		
		Thread oddThread = new Thread(odds);
		Thread evenThread = new Thread(evens);
		Thread heartbeatThread = new Thread(heart);
		
		heartbeatThread.start();
		evenThread.start();
		oddThread.start();
		
		try {
			
			// As I mentioned in Slack, the join() method makes more sense if you think
			// of it as stopAndWaitForThisThreadToFinish().
			// So we first wait for the evenThread to finish. If that thread is already
			// finished when we call join(), the method immediately returns, and we 
			// move on to the next line. If not, the main thread sleeps until the OS
			// tells us that it's finished. The same then happens for the oddThread.
			evenThread.join();
			oddThread.join();
			
			// There are two ways to stop the heartbeat thread. The first is to call
			// the method that sets our boolean flag, then call join() to wait for the
			// thread to end. The scond is to call interrupt, which will trigger the 
			// exception when heartbeat finishes sleeping. Then we call join() to wait
			// for it to finish.
			//
			// Note that interrupt() does not guarantee that the target thread will
			// end. The target thread can ignore the interrupt, or not see it at all,
			// depending on what it's doing.
			//
			// More info here: 
			// http://stackoverflow.com/questions/3590000/what-does-java-lang-thread-interrupt-do
			
			// Option 1
			//heart.stopBeating();	
			//heartbeatThread.join();
			
			// Option 2
			heartbeatThread.interrupt();
			heartbeatThread.join();
		}
		catch(InterruptedException e) {}

		System.out.println("All finished");
	}
}