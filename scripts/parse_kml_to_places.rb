#encoding: utf-8
$places = []
# Actual record ID, default 0
$act_id = 0
# Actual category ID, default 0
$act_cat = 0
# $act_area
# 1 => PL/Lubelskie
# 2 => PL/Podlaskie
# 3 => PL/Podkarpackie

$act_area = 1
# $act_author
# 1 => Narzew
# 2 => A.G.
# More will be added
$act_author = 2
# $act_active
# 0 => Active
# 1 => Inactive
# 2 => Marked as bad
$act_active = 1
data = File.read('KMLData.kml', :encoding=>"UTF-8")
places_unparsed = data.split("Placemark")[1..-1]
places_unparsed.each{|x|
	next unless x.include?("<name>") && x.include?("<coordinates>")
	# Parse special sequence: setcategoryid
	if x.include?("<setcategoryid>")
		$act_cat = x.split("<setcategoryid>")[1].split("</setcategoryid>")[0].to_i
		print "Changed current category to #{$act_cat}\n"
	end
	# Parse special sequence: setareaid
	if x.include?("<setareaid>")
		$act_area = x.split("<setareaid>")[1].split("</setareaid>")[0].to_i
		print "Changed current area to #{$act_area}\n"
	end
	# Parse special sequence: setuathorid
	if x.include?("<setauthorid>")
		$act_author = x.split("<setauthorid>")[1].split("</setauthorid>")[0].to_i
		print "Changed current author to #{$act_author}\n"
	end
	# Parse special sequence: setactive
	if x.include?("<setactive>")
		$act_active = x.split("<setactive>")[1].split("</setactive>")[0].to_i
		print "Changed current active status to #{$act_active}\n"
	end
	
	name = x.split("<name>")[1].split("</name>")[0].strip
	description = lambda {
		return x.split("<description>")[1].split("</description>")[0]
	}.call rescue description = ""
	if description.include?("<br/>")
		description = description.split("<br/>")[-1]
	end
	if description.include?("<br>")
		description = description.split("<br>")[-1]
	end
	if description.include?("<img src")
		description = ""
	end
	description = description.gsub(" ]]>","").gsub("]]>","").gsub(" ","\x20")
	description = description.strip
	description = description.gsub("\'","\\\'")
	coordinates = x.split("<coordinates>")[1].split("</coordinates>")[0].strip
	a = coordinates.split(",")
	point_x = a[0].strip
	point_y = a[1].strip
	$places << [name,description,point_x,point_y, $act_cat, $act_area, $act_active, $act_author]
}
s = "insert into places(name,description,lat,lng,cat,area,active,author) values "
$places.each{|x|
	s << "(\'#{x[0]}\', \'#{x[1]}\', #{x[2]}, #{x[3]}, #{x[4]}, #{x[5]}, #{x[6]}, #{x[7]}),\n"
}
s[-2] = ";"
File.open("result.sql","wb"){|w| w.write(s) }
