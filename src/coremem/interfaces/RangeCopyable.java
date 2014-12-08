package coremem.interfaces;

public interface RangeCopyable<M>
{
	public void copyRangeTo(long pSourceOffset,
													M pTo,
													long pDestinationOffset,
													long pLengthToCopy);
}
