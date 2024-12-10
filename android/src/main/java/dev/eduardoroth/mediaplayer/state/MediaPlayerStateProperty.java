package dev.eduardoroth.mediaplayer.state;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class MediaPlayerStateProperty<T> {
    private final MutableLiveData<T> _property;
    private final LifecycleOwner _owner;
    private boolean _setOnce = false;
    private boolean _pristine = true;

    public MediaPlayerStateProperty(LifecycleOwner owner) {
        this(owner, null, false);
    }

    public MediaPlayerStateProperty(LifecycleOwner owner, T defaultValue) {
        this(owner, defaultValue, false);
    }

    public MediaPlayerStateProperty(LifecycleOwner owner, T defaultValue, boolean setOnce) {
        _property = new MutableLiveData<>(defaultValue);
        _owner = owner;
        _setOnce = setOnce;
    }

    public T get() {
        return _property.getValue();
    }

    public void set(T value) {
        if (_setOnce && !_pristine) {
            return;
        }
        if (_property.getValue() != value) {
            _property.setValue(value);
            _pristine = false;
        }
    }

    public void post(T value) {
        if (_setOnce && !_pristine) {
            return;
        }
        if (_property.getValue() != value) {
            _property.postValue(value);
            _pristine = false;
        }
    }

    public void observe(
            @NonNull Observer<? super T> observer
    ) {
        _property.observe(_owner, observer);
    }

}
