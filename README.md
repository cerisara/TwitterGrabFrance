This is a very simple adaptation of https://github.com/twitter/hbc
to continuously grab tweets from all over France, without using any search term
but using a polygon (actually a square) all over GPS coordinates of France.

To use, you first need to define your OAuth application keys and secrets and put them
in a file: look at line 62 of class TwitterGrab.java
You should also change the hard-coded (sorry, I know it's bad programming practice, but
honestly, the code is soo simple, anyone can just edit it) filename at line 62.

If you want another country, also change the GPS coordinates at line 48.

Then just compile with

ant jar

and run it with

java -jar twittergrab.jar > output.json

