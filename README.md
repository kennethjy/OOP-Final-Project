# OOP-Final-Project

## Brief Description
The program I made is a rhythm game with 4 types of notes: tap notes, hold notes, flick notes, and slide notes. Tap notes and hold notes appear in 4 lanes. There are also slide notes, which have to be caught, but there is no need to click the keys, the keys just need to be held down. Flick notes are the same as tap notes, except you have to also slide your finger upwards to a higher key at the same lane.
The keys corresponding to each lane is the entire keyboard, with the Z key being the first lane and the period key being the last lane. The keys above have their corresponding lanes and heights.
This allows for gameplay to be more flexible depending on the note charter, as for people who have never played it, gameplay is very lenient. However, the chart can be made harder, allowing for more challenging gameplay for people with a lower skill level.

## Class Diagram
https://miro.com/welcomeonboard/bUlZTjJjS2tCQzRGWVdjRDZQM2d3NUN6Wk1zR01vcjlnVGhxaFl3dTVJbEh2azF4ZTliZ0FsUWZsV0tZcWRtSXwzNDU4NzY0NTQ2Njg0NDM3NDUwfDI=?share_link_id=228964996273

## Libraries
JavaFX
A library containing basic GUI functions. I used this module to play the music, handle inputs, handle events and update the objects on the game window

## Algorithms and Data Structures used
One of the algorithms I used was to transpose the 2D coordinates into 3D coordinates. The formula is:
pow((1000 - time) / 1000.0, 2) * ((midpointBottom - 0.5 + (width / 2.0)) * sWidth / 4 - vanishX) + vanishX;

Basically, a percentage between the top of the screen and the bottom of the screen in 2D is taken. Then, it is squared and applied to 2 points: the vanishing point, and the point at the bottom of the screen at that lane. This creates a 3D looking image with 1 point perspective. Also, the square is there to make the animation look more natural.

The data structure I used to store the notes is ArrayList. The program goes through each note in the list and animates it depending on the time from the previous frame. This allows the game to run at the maximum FPS that the computer can handle.

## Screenshots

