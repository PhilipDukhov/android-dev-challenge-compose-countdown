# Put title of your app here

![Workflow result](https://github.com/PhilipDukhov/android-dev-challenge-compose-countdown/workflows/Check/badge.svg)


## :scroll: Description
This is a simple timer - on the first screen you can choose a duration - click on hours/minutes/seconds to edit it.
After pressing start button timer will start. 
While it's going there's a processing animation.
When it ends, the device will vibrate.

## :bulb: Motivation and Context
NextSecondNotifier handles timer update every second
Not sure if there's a better solution to animate value for a circle - I ended up creating Animatable and animating it's value inside remember, but it doesn't looks great. That's why I've created an Animatable constructor for case where I need to start animation on first render. I had to use MainScope(), which if probably fine but not ideal.


## :camera_flash: Screenshots
<img src="/results/screenshot_1.png" width="260">&emsp;<img src="/results/screenshot_2.png" width="260">

## License
```
Copyright 2020 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```