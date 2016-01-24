package bump3.engines;
import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;
/* Plug

artist and title variable format:
    left][right][start

url=http://www.seekasong.com/search.php?artist=$a$&title=$t$&what=1&submit.x=21&submit.y=18

istart=\>Download</a></td>][0
loop

a=<a href="/mp3/][">][9
t= - ][<][3

trim=a][mp3
trim=a][<b>
trim=a][</b>
trim=a][[^a-zA-Z0-9]
trim=t][mp3
trim=t][<b>
trim=t][</b>
trim=t][[^a-zA-Z0-9]
comp=r][at][$a$$t$
comp=e][t][$t$
pageurl=http://www.seekasong.com/search.php?artist=$a$&title=$t$&what=$p$&submit.x=21&submit.y=18
pagestart=1
pageinc=1
*/

public class Plug {
	public static int search(String artist, String title) {
		return 0;
	}
}