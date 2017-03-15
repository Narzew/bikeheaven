require 'find'
require 'json'
require 'zlib'
$elev_table = []
Find.find('elevations').each{|x|
	next if File.directory?(x)
	next unless x.split(".")[-1] == "json"
	print "Parsing #{x}..\n"
	data = JSON.parse(File.read(x))
	$elev_table << [data["results"]]
}
print "Writing data..\n"
File.open("elevations.ncp","wb"){|w| w.write(Zlib::Deflate.deflate(Marshal.dump($elev_table)))}
print "Done.\n"
