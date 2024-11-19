# @eduardoroth/media-player

Native Media Player for iOS, Android and Browser.
Based on the great work of [@jepiqueau](https://github.com/jepiqueau)

## Install

```bash
npm install @eduardoroth/media-player
npx cap sync
```

## API

<docgen-index>

* [`create(...)`](#create)
* [`play(...)`](#play)
* [`pause(...)`](#pause)
* [`getDuration(...)`](#getduration)
* [`getCurrentTime(...)`](#getcurrenttime)
* [`setCurrentTime(...)`](#setcurrenttime)
* [`isPlaying(...)`](#isplaying)
* [`isMuted(...)`](#ismuted)
* [`mute(...)`](#mute)
* [`getVolume(...)`](#getvolume)
* [`setVolume(...)`](#setvolume)
* [`getRate(...)`](#getrate)
* [`setRate(...)`](#setrate)
* [`remove(...)`](#remove)
* [`removeAll()`](#removeall)
* [`addListener('MediaPlayer:Ready', ...)`](#addlistenermediaplayerready-)
* [`addListener('MediaPlayer:Play', ...)`](#addlistenermediaplayerplay-)
* [`addListener('MediaPlayer:Pause', ...)`](#addlistenermediaplayerpause-)
* [`addListener('MediaPlayer:Ended', ...)`](#addlistenermediaplayerended-)
* [`addListener('MediaPlayer:Removed', ...)`](#addlistenermediaplayerremoved-)
* [`addListener('MediaPlayer:Seeked', ...)`](#addlistenermediaplayerseeked-)
* [`addListener('MediaPlayer:TimeUpdate', ...)`](#addlistenermediaplayertimeupdate-)
* [`addListener('MediaPlayer:FullScreen', ...)`](#addlistenermediaplayerfullscreen-)
* [`addListener('MediaPlayer:PictureInPicture', ...)`](#addlistenermediaplayerpictureinpicture-)
* [`removeAllListeners(...)`](#removealllisteners)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### create(...)

```typescript
create(options: MediaPlayerOptions) => Promise<MediaPlayerResult<string>>
```

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayeroptions">MediaPlayerOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;string&gt;&gt;</code>

--------------------


### play(...)

```typescript
play(options: MediaPlayerIdOptions) => Promise<MediaPlayerResult<string>>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayeridoptions">MediaPlayerIdOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;string&gt;&gt;</code>

--------------------


### pause(...)

```typescript
pause(options: MediaPlayerIdOptions) => Promise<MediaPlayerResult<string>>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayeridoptions">MediaPlayerIdOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;string&gt;&gt;</code>

--------------------


### getDuration(...)

```typescript
getDuration(options: MediaPlayerIdOptions) => Promise<MediaPlayerResult<number>>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayeridoptions">MediaPlayerIdOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;number&gt;&gt;</code>

--------------------


### getCurrentTime(...)

```typescript
getCurrentTime(options: MediaPlayerIdOptions) => Promise<MediaPlayerResult<number>>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayeridoptions">MediaPlayerIdOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;number&gt;&gt;</code>

--------------------


### setCurrentTime(...)

```typescript
setCurrentTime(options: MediaPlayerSetCurrentTimeOptions) => Promise<MediaPlayerResult<number>>
```

| Param         | Type                                                                                          |
| ------------- | --------------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayersetcurrenttimeoptions">MediaPlayerSetCurrentTimeOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;number&gt;&gt;</code>

--------------------


### isPlaying(...)

```typescript
isPlaying(options: MediaPlayerIdOptions) => Promise<MediaPlayerResult<boolean>>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayeridoptions">MediaPlayerIdOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;boolean&gt;&gt;</code>

--------------------


### isMuted(...)

```typescript
isMuted(options: MediaPlayerIdOptions) => Promise<MediaPlayerResult<boolean>>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayeridoptions">MediaPlayerIdOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;boolean&gt;&gt;</code>

--------------------


### mute(...)

```typescript
mute(options: MediaPlayerIdOptions) => Promise<MediaPlayerResult<boolean>>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayeridoptions">MediaPlayerIdOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;boolean&gt;&gt;</code>

--------------------


### getVolume(...)

```typescript
getVolume(options: MediaPlayerIdOptions) => Promise<MediaPlayerResult<number>>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayeridoptions">MediaPlayerIdOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;number&gt;&gt;</code>

--------------------


### setVolume(...)

```typescript
setVolume(options: MediaPlayerSetVolumeOptions) => Promise<MediaPlayerResult<number>>
```

| Param         | Type                                                                                |
| ------------- | ----------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayersetvolumeoptions">MediaPlayerSetVolumeOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;number&gt;&gt;</code>

--------------------


### getRate(...)

```typescript
getRate(options: MediaPlayerIdOptions) => Promise<MediaPlayerResult<number>>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayeridoptions">MediaPlayerIdOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;number&gt;&gt;</code>

--------------------


### setRate(...)

```typescript
setRate(options: MediaPlayerSetRateOptions) => Promise<MediaPlayerResult<number>>
```

| Param         | Type                                                                            |
| ------------- | ------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayersetrateoptions">MediaPlayerSetRateOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;number&gt;&gt;</code>

--------------------


### remove(...)

```typescript
remove(options: MediaPlayerIdOptions) => Promise<MediaPlayerResult<string>>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#mediaplayeridoptions">MediaPlayerIdOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;string&gt;&gt;</code>

--------------------


### removeAll()

```typescript
removeAll() => Promise<MediaPlayerResult<string[]>>
```

**Returns:** <code>Promise&lt;<a href="#mediaplayerresult">MediaPlayerResult</a>&lt;string[]&gt;&gt;</code>

--------------------


### addListener('MediaPlayer:Ready', ...)

```typescript
addListener(event: 'MediaPlayer:Ready', listener: (event: { playerId: string; }) => void) => Promise<PluginListenerHandle>
```

| Param          | Type                                                   |
| -------------- | ------------------------------------------------------ |
| **`event`**    | <code>'MediaPlayer:Ready'</code>                       |
| **`listener`** | <code>(event: { playerId: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('MediaPlayer:Play', ...)

```typescript
addListener(event: 'MediaPlayer:Play', listener: (event: { playerId: string; }) => void) => Promise<PluginListenerHandle>
```

| Param          | Type                                                   |
| -------------- | ------------------------------------------------------ |
| **`event`**    | <code>'MediaPlayer:Play'</code>                        |
| **`listener`** | <code>(event: { playerId: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('MediaPlayer:Pause', ...)

```typescript
addListener(event: 'MediaPlayer:Pause', listener: (event: { playerId: string; }) => void) => Promise<PluginListenerHandle>
```

| Param          | Type                                                   |
| -------------- | ------------------------------------------------------ |
| **`event`**    | <code>'MediaPlayer:Pause'</code>                       |
| **`listener`** | <code>(event: { playerId: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('MediaPlayer:Ended', ...)

```typescript
addListener(event: 'MediaPlayer:Ended', listener: (event: { playerId: string; }) => void) => Promise<PluginListenerHandle>
```

| Param          | Type                                                   |
| -------------- | ------------------------------------------------------ |
| **`event`**    | <code>'MediaPlayer:Ended'</code>                       |
| **`listener`** | <code>(event: { playerId: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('MediaPlayer:Removed', ...)

```typescript
addListener(event: 'MediaPlayer:Removed', listener: (event: { playerId: string; }) => void) => Promise<PluginListenerHandle>
```

| Param          | Type                                                   |
| -------------- | ------------------------------------------------------ |
| **`event`**    | <code>'MediaPlayer:Removed'</code>                     |
| **`listener`** | <code>(event: { playerId: string; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('MediaPlayer:Seeked', ...)

```typescript
addListener(event: 'MediaPlayer:Seeked', listener: (event: { playerId: string; previousTime: number | undefined; newTime: number; }) => void) => Promise<PluginListenerHandle>
```

| Param          | Type                                                                                          |
| -------------- | --------------------------------------------------------------------------------------------- |
| **`event`**    | <code>'MediaPlayer:Seeked'</code>                                                             |
| **`listener`** | <code>(event: { playerId: string; previousTime: number; newTime: number; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('MediaPlayer:TimeUpdate', ...)

```typescript
addListener(event: 'MediaPlayer:TimeUpdate', listener: (event: { playerId: string; currentTime: number; }) => void) => Promise<PluginListenerHandle>
```

| Param          | Type                                                                        |
| -------------- | --------------------------------------------------------------------------- |
| **`event`**    | <code>'MediaPlayer:TimeUpdate'</code>                                       |
| **`listener`** | <code>(event: { playerId: string; currentTime: number; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('MediaPlayer:FullScreen', ...)

```typescript
addListener(event: 'MediaPlayer:FullScreen', listener: (event: { playerId: string; isInFullScreen: boolean; }) => void) => Promise<PluginListenerHandle>
```

| Param          | Type                                                                            |
| -------------- | ------------------------------------------------------------------------------- |
| **`event`**    | <code>'MediaPlayer:FullScreen'</code>                                           |
| **`listener`** | <code>(event: { playerId: string; isInFullScreen: boolean; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('MediaPlayer:PictureInPicture', ...)

```typescript
addListener(event: 'MediaPlayer:PictureInPicture', listener: (event: { playerId: string; isInPictureInPicture: boolean; }) => void) => Promise<PluginListenerHandle>
```

| Param          | Type                                                                                  |
| -------------- | ------------------------------------------------------------------------------------- |
| **`event`**    | <code>'MediaPlayer:PictureInPicture'</code>                                           |
| **`listener`** | <code>(event: { playerId: string; isInPictureInPicture: boolean; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### removeAllListeners(...)

```typescript
removeAllListeners(playerId: string) => Promise<void>
```

| Param          | Type                |
| -------------- | ------------------- |
| **`playerId`** | <code>string</code> |

--------------------


### Interfaces


#### Error

| Prop          | Type                |
| ------------- | ------------------- |
| **`name`**    | <code>string</code> |
| **`message`** | <code>string</code> |
| **`stack`**   | <code>string</code> |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


### Type Aliases


#### MediaPlayerResult

<code>{ method: string; result: boolean; value?: ResultValueType; error?: <a href="#error">Error</a>; message?: string; }</code>


#### MediaPlayerOptions

<code>{ playerId: string; url: string; ios?: <a href="#mediaplayeriosoptions">MediaPlayerIosOptions</a>; android?: <a href="#mediaplayerandroidoptions">MediaPlayerAndroidOptions</a>; web?: <a href="#mediaplayerweboptions">MediaPlayerWebOptions</a>; extra?: { title?: string; subtitle?: string; poster?: string; artist?: string; rate?: number; subtitles?: <a href="#mediaplayersubtitleoptions">MediaPlayerSubtitleOptions</a>; loopOnEnd?: boolean; showControls?: boolean; headers?: { [key: string]: string; }; }; }</code>


#### MediaPlayerIosOptions

<code>{ enableExternalPlayback?: boolean; enablePiP?: boolean; enableBackgroundPlay?: boolean; openInFullscreen?: boolean; automaticallyEnterPiP?: boolean; top?: number; left?: number; height?: number; width?: number; }</code>


#### MediaPlayerAndroidOptions

<code>{ enableChromecast?: boolean; enablePiP?: boolean; enableBackgroundPlay?: boolean; openInFullscreen?: boolean; top?: number; left?: number; height?: number; width?: number; }</code>


#### MediaPlayerWebOptions

<code>{ enableChromecast?: boolean; }</code>


#### MediaPlayerSubtitleOptions

<code>{ url: string; options?: { language?: string; foregroundColor?: string; backgroundColor?: string; fontSize?: number; }; }</code>


#### MediaPlayerIdOptions

<code>{ playerId: string; }</code>


#### MediaPlayerSetCurrentTimeOptions

<code>{ playerId: string; time: number; }</code>


#### MediaPlayerSetVolumeOptions

<code>{ playerId: string; volume: number; }</code>


#### MediaPlayerSetRateOptions

<code>{ playerId: string; rate: number; }</code>

</docgen-api>