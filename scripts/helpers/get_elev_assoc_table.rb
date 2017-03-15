require 'zlib'
$associated_table = {}
print "Reading data..\n"
$elev_table = Marshal.load(Zlib::Inflate.inflate(File.binread('elevations.ncp')))
count = 0
max_count = $elev_table.size
$elev_table.each{|x|
	lambda {
		count += 1
		x.each{|y|
			lambda{
				y.each{|z|
					lambda{
						elevation = z["elevation"]
						lat = z["location"]["lat"].round(8)
						lng = z["location"]["lng"].round(8)
						lat_lng = "#{lat}_#{lng}"
						$associated_table[lat_lng] = elevation
						percent = ((count.to_f/max_count.to_f)*100).floor
						print "#{percent}%: #{lat},#{lng} => #{elevation}\n"
					}.call rescue print "Error!\n"
				}
			}.call rescue print "ERROR!\n"
		}
	}.call rescue print "ERROR!\n"
}
print "Writing data..\n"
File.open("elevations_associated.ncp","wb"){|w| w.write(Zlib::Deflate.deflate(Marshal.dump($associated_table)))}
print "Done.\n"
exit
