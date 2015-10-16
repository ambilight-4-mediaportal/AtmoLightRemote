CWAC ColorMixer: Appealing To Your Users' Sense of Fashion
==========================================================
Sometimes, you want your users to pick a color. A simple
approach is to give the user a fixed roster of a handful
of colors -- easy, but limited. A fancy approach is to use
some form of color wheel, but these can be difficult to use
on a touchscreen and perhaps impossible without a touchscreen.

`ColorMixer` is a widget that provides a simple set of `SeekBars`
to let the user mix red, green, and blue to pick an arbitrary color.
It is not very big, so it is easy to fit on a form, and it is still
fairly finger-friendly.

It is also packaged as a dialog (`ColorMixerDialog`), a dialog-themed
activity (`ColorMixerActivity`), and a preference (`ColorPreference`).

This is distributed as an Android library project. You can use it
in source form or via an AAR.

Usage
-----

### Obtaining the AAR

Add the following blocks to your `build.gradle` file:

```groovy
repositories {
    maven {
        url "https://s3.amazonaws.com/repo.commonsware.com"
    }
}

dependencies {
    compile 'com.commonsware.cwac:colormixer:0.6.+'
}
```

Or, if you cannot use SSL, use `http://repo.commonsware.com` for the repository
URL.

### ColorMixer

`ColorMixer` is a simple widget. Given that you have the parcel
installed in your project, or have manually merged the source
and resources into your project, you can add the widget to a
layout like any other:

	<com.commonsware.cwac.colormixer.ColorMixer
		android:id="@+id/mixer"
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
	/>

You can call `getColor()` and `setColor()` to manipulate the
color at runtime. You can also call `setOnColorChangedListener()`
to register a `ColorMixer.OnColorChangedListener` object, which
will be called with `onColorChanged()` when the color is altered
by the user.

There is one custom attribute, `cwac_colormixer_color`, that you can use, to set
the initial color (instead of using `setColor()`). To use this
custom attribute, add the
`xmlns:mixer="http://schemas.android.com/apk/res-auto"` namespace
declaration to your layout, then add the `mixer:cwac_colormixer_color` attribute
to the `com.commonsware.cwac.colormixer.ColorMixer` widget:

  <com.commonsware.cwac.colormixer.ColorMixer
    android:id="@+id/mixer"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    mixer:cwac_colormixer_color="#FFFF00FF"
  />

### ColorMixerDialog

`ColorMixerDialog` is an `AlertDialog` subclass. Hence, to create
and show the dialog, all you need to do is create an instance
and `show()` it:

	new ColorMixerDialog(this, someColor, onDialogSet).show();

In the above code snippet, `this` is a `Context` (e.g., an `Activity`),
`someColor` is the color you want to start with, and `onDialogSet`
is a `ColorMixer.OnColorChangedListener` that will be notified
*if* the user clicks the "Set" button on the dialog *and* has
changed the color from the initial value.

### ColorMixerActivity

`ColorMixerActivity` is a dialog-themed `Activity`. This is
useful for situations where you want a dialog but do not want
to deal with a dialog.

To use it, add it as an activity to your project. You will
need to use the full package in your `<activity>` element,
marking it as using `Theme.Dialog`.
Here is one implementation, from the `demo/` project:

	<activity android:name="com.commonsware.cwac.colormixer.ColorMixerActivity"
					android:label="@string/app_name"
					android:theme="@android:style/Theme.Dialog">
	</activity>

In the `Intent` you use to start the activity, you can supply
the starting color via a `ColorMixerActivity.COLOR` integer
extra, and the dialog title via a `ColorMixerActivity.TITLE`
string extra. For example:

	Intent i=new Intent(this, ColorMixerActivity.class);
	
	i.putExtra(ColorMixerActivity.TITLE, "Pick a Color");
	i.putExtra(ColorMixerActivity.COLOR, mixer.getColor());
	
	startActivityForResult(i, COLOR_REQUEST);

### ColorPreference

`ColorPreference` is a `Preference` class, to be referenced
in preference XML and loaded into a `PreferenceActivity`. It
has no attributes beyond the standard ones.

	<PreferenceScreen
		xmlns:android="http://schemas.android.com/apk/res/android">
		<com.commonsware.cwac.colormixer.ColorPreference
			android:key="favoriteColor"
			android:defaultValue="0xFFA4C639"
			android:title="Your Favorite Color"
			android:summary="Blue.  No yel--  Auuuuuuuugh!" />
	</PreferenceScreen>

The preference is stored as an integer under the key you
specify in the XML.

Dependencies
------------
This project has no dependencies.

This project should work on API Level 4 and higher &mdash; please report
bugs if you find otherwise.

Version
-------
This is version v0.6.0 of this module, meaning it is creeping
towards respectability.

**NOTE**: To eliminate a collision on the attribute name with Android 5.0, users
of v0.5.3 or older versions of this library will need to upgrade
their layout XML to reference `cwac_colormixer_color` instead
of just `color`.

Demo
----
There is a `demo/` directory containing a demo project. It uses
the library project itself to access the source code and
resources of the `ColorMixer` library. When built using Eclipse, it depends
upon the library being located at `../colormixer` with respect to the
demo project. When built using Gradle/Android Studio, it uses the
local `:colormixer` project for debug builds and the AAR artifact
for release builds.

License
-------
The code in this project is licensed under the Apache
Software License 2.0, per the terms of the included LICENSE
file.

Questions
---------
If you have questions regarding the use of this code, please post a question
on [StackOverflow](http://stackoverflow.com/questions/ask) tagged with
`commonsware-cwac` and `android` after [searching to see if there already is an answer](https://stackoverflow.com/search?q=[commonsware-cwac]+colormixer). Be sure to indicate
what CWAC module you are having issues with, and be sure to include source code 
and stack traces if you are encountering crashes.

If you have encountered what is clearly a bug, please post an [issue](https://github.com/commonsguy/cwac-colormixer/issues). Be certain to include complete steps
for reproducing the issue.

Do not ask for help via Twitter.

Release Notes
-------------
- v0.6.1: updated for Android Studio 1.0 and new AAR publishing system
- v0.6.0: renamed attribute from `color` to `cwac_colormixer_color`, added more Gradle
- v0.5.3: manifest fixes to help with manifest merging
- v0.5.2: continued work to improve IDE preview panes
- v0.5.1: bug fix to help with IDE preview panes
- v0.5.0: added Gradle support
- v0.4.3: removed `CWAC-Parcel` dependency
- v0.4.2: demonstrated better Android library project integration with layouts
- v0.4.1: fixed `ColorPreference` to work better on Honeycomb
- v0.4.0: converted to Android library project, added ColorMixerActivity

Who Made This?
--------------
<a href="http://commonsware.com">![CommonsWare](http://commonsware.com/images/logo.png)</a>

[gg]: http://groups.google.com/group/cw-android
