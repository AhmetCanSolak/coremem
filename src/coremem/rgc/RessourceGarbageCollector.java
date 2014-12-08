package coremem.rgc;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RessourceGarbageCollector
{

	private static final Executor sExecutor = Executors.newSingleThreadExecutor();

	private static final ScheduledExecutorService sScheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	private static RessourceGarbageCollector sRessourceGarbageCollector;

	static
	{
		sRessourceGarbageCollector = new RessourceGarbageCollector();

		sRessourceGarbageCollector.collectAtFixedRate(1, TimeUnit.MINUTES);
	}

	private static ConcurrentLinkedDeque<CleaningPhantomReference> sCleaningPhantomReferenceList = new ConcurrentLinkedDeque<>();

	public static final void register(Cleanable pCleanable)
	{
		CleaningPhantomReference lCleaningPhantomReference = new CleaningPhantomReference(pCleanable,
																																											pCleanable.getCleaner(),
																																											sRessourceGarbageCollector.getReferenceQueue());

		sCleaningPhantomReferenceList.add(lCleaningPhantomReference);
	}

	private final ReferenceQueue<Cleanable> mReferenceQueue = new ReferenceQueue<>();

	private ReferenceQueue<Cleanable> getReferenceQueue()
	{
		return mReferenceQueue;
	}

	private final AtomicBoolean mActive = new AtomicBoolean(true);

	private void collect()
	{
		if (mActive.get())
			do
			{
				CleaningPhantomReference lReference = (CleaningPhantomReference) mReferenceQueue.poll();
				if (lReference == null)
					return;
				Cleaner lCleaner = lReference.getCleaner();
				if (lCleaner != null)
					sExecutor.execute(lCleaner);
				sCleaningPhantomReferenceList.remove(lReference);
			}
			while (true);
	}

	public void collectAtFixedRate(long pPeriod, TimeUnit pUnit)
	{
		Runnable lCollector = new Runnable()
		{
			@Override
			public void run()
			{
				collect();
			}
		};
		sScheduledExecutor.scheduleAtFixedRate(	lCollector,
																						0,
																						pPeriod,
																						pUnit);
	}

	public static void collectNow()
	{
		sRessourceGarbageCollector.collect();
	}

	public static void preventCollection(Runnable pRunnable)
	{
		final boolean lActive = sRessourceGarbageCollector.mActive.get();
		sRessourceGarbageCollector.mActive.set(false);
		pRunnable.run();
		sRessourceGarbageCollector.mActive.compareAndSet(false, lActive);
	}

	public static int getNumberOfRegisteredObjects()
	{
		return sCleaningPhantomReferenceList.size();
	}

}
