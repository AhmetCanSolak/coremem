package coremem.interfaces;

import org.bridj.Pointer;

public interface BridJPointerWrappable<T>
{
	public Pointer<T> getBridJPointer(Class<T> pTargetClass);
}
