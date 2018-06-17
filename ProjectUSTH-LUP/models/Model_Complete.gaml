/**
* Name: session 7
* Author: 
* Description: Model representing the sanility diffusion among the parcels of Binh Thanh village
*                      taking into account the different dikes and sluices
* Tags: 
*/
model session7


global
{
	file parcel_shapefile <- shape_file("../includes/binhthanh_village_scale/parcels_binhthanh_village.shp");
	file river_shapefile <- shape_file("../includes/binhthanh_village_scale/rivers_chanels_dikes/rivers_binhthanh.shp");
	file sluice_shapefile <- shape_file("../includes/binhthanh_village_scale/rivers_chanels_dikes/sluice_binhthanh.shp");
	file dike_shapefile <- shape_file("../includes/binhthanh_village_scale/rivers_chanels_dikes/dikes_binhthanh.shp");
	file csv_lut <- csv_file("../includes/csv_datasets/lut.csv", true);
	file csv_price <- csv_file("../includes/csv_datasets/price.csv", false);
	file csv_suitability <- csv_file("../includes/csv_datasets/suitability_case.csv", true);
	file csv_transition <- csv_file("../includes/csv_datasets/transition.csv", false);
	file csv_cost <- csv_file("../includes/csv_datasets/cost.csv", false);
	file csv_investment <- csv_file("../includes/csv_datasets/investment.csv", false);
	float max_salinity <- 12.0;
	float min_salinity <- 2.0;
	float max_price;
	float max_cost;
	float max_investment;
	float max_delay;
	map<string, rgb> color_map <- ["BHK"::# darkgreen, "LNC"::# lightgreen, "TSL"::# orange, "LNQ"::# brown, "LUC"::# lightyellow, "LUK"::# gold, "LTM"::# cyan, "LNK"::# red];
	date starting_date <- date([2005, 1, 1, 0, 0, 0]);
	river the_main_river;
	float step <- 1 # month;
	geometry shape <- envelope(parcel_shapefile);
	list<sluice> openSluice;
	list<parcel> parcels_not_diked update: parcel where (not each.diked);
	bool rain_season <- false update: current_date.month >= 6 and current_date.month < 11;
	float weight_profit <- 0.5 parameter: true;
	float weight_risk <- 0.5 parameter: true;
	float weight_implementation <- 0.5 parameter: true;
	float weight_suitability <- 0.5 parameter: true;
	float weight_neighborhood <- 0.5 parameter: true;
	float weight_investment <- 0.5 parameter: true;
	float weight_cost <- 0.5 parameter: true;
	float weight_delay <- 0.5 parameter: true;
	list criteria <- [
		["name"::"profit", "weight"::weight_profit], 
		["name"::"risk", "weight"::weight_risk], 
		["name"::"implementation", "weight"::weight_implementation], 
		["name"::"suitability", "weight"::weight_suitability], 
		["name"::"neigboorhood", "weight"::weight_neighborhood],
		["name"::"investment", "weight"::weight_investment],
		["name"::"cost", "weight"::weight_cost],
		["name"::"delay", "weight"::weight_delay]
	];
	float probability_changing <- 0.5 parameter: true;
	action load_land_use
	{
		create land_use from: csv_lut with: [
			name:: get("landuse"), 
			lu_code::get("lu_code"), 
			average_yield_ha::float(get("avg_yield_ha")), 
			risk::float(get("risk")),
			delay::float(get("delay_month"))
		];
		max_delay <- max (land_use accumulate (each.delay));
	}

	action load_price
	{
		matrix<string> data <- csv_price.contents;
		loop lu_row from: 1 to: data.rows - 1
		{
			string lu_code <- data[0, lu_row];
			land_use concerned <- first(land_use where (each.lu_code = lu_code));
			loop year from: 1 to: data.columns - 1
			{
				add float(data[year, lu_row]) to: concerned.price_map at: (year - 1) + 2005;
			}

		}

		max_price <- max(land_use accumulate (each.price_map.values));
	}
	
	action load_cost
	{
		matrix<string> data <- csv_cost.contents;
		loop lu_row from: 1 to: data.rows - 1
		{
			string lu_code <- data[0, lu_row];
			land_use concerned <- first(land_use where (each.lu_code = lu_code));
			loop year from: 1 to: data.columns - 1
			{
				add float(data[year, lu_row]) to: concerned.cost_map at: (year - 1) + 2005;
			}

		}

		max_cost <- max(land_use accumulate (each.cost_map.values));
	}
	
	action load_investment
	{
		matrix<string> data <- csv_investment.contents;
		loop lu_row from: 1 to: data.rows - 1
		{
			string lu_code <- data[0, lu_row];
			land_use concerned <- first(land_use where (each.lu_code = lu_code));
			loop year from: 1 to: data.columns - 1
			{
				add float(data[year, lu_row]) to: concerned.investment_map at: (year - 1) + 2005;
			}

		}

		max_investment <- max(land_use accumulate (each.investment_map.values));
	}

	action load_transition
	{
		matrix<string> data <- csv_transition.contents;
		loop lu_row from: 1 to: data.rows - 1
		{
			string lu_code <- data[0, lu_row];
			land_use concerned <- first(land_use where (each.lu_code = lu_code));
			loop col from: 1 to: data.columns - 1
			{
				add int(data[col, lu_row]) to: concerned.transition_map at: data[col, 0];
			}

		}

	}

	action load_suitability
	{
		create suitability_case from: csv_suitability with:
		[soil_type::get("soiltType"), acidity::get("acid_depth"), salinity::int(get("salinity")), lu_code::get("lu_code"), suitability::int(get("landsuitability"))];
	}

	action load_parcel
	{
		create parcel from: parcel_shapefile
		{
			add read("lu05") to: lu_years at: 2005;
			add read("lu10") to: lu_years at: 2010;
			add read("lu14") to: lu_years at: 2014;
			soil_type <- read("soil");
			acidity <- read("acid_d");
			add float(read("sal_05")) to: salinity_years at: 2005;
			add float(read("sal_10")) to: salinity_years at: 2010;
			add float(read("sal_14")) to: salinity_years at: 2014;
			my_land_use <- first(land_use where (each.lu_code = lu_years[2005]));
			current_salinity <- salinity_years[2005];
			create farmer with: [my_parcel::self, location::location];
		}

		ask parcel
		{
			neighborhood <- parcel at_distance (10);
		}

		ask farmer
		{
			neighborhood <- farmer at_distance (200);
		}

		max_salinity <- max(parcel accumulate each.salinity_years.values);
		min_salinity <- min(parcel accumulate each.salinity_years.values);
	}

	action load_river
	{
		create river from: river_shapefile with: [is_main_river::(get("MAIN") = "T")];
		the_main_river <- river first_with each.is_main_river;
	}

	action load_dike
	{
		create dike from: dike_shapefile
		{
			contactParcel <- parcel at_distance (200);
			contactRiver <- river where (not each.is_main_river and (each.shape intersects (self.shape)));
			ask (contactParcel)
			{
				diked <- true;
			}

			ask (contactRiver)
			{
				diked <- true;
			}

		}

	}

	action load_sluice
	{
		create sluice from: sluice_shapefile with: [open::(read("OPEN") = "T")]
		{
			if (open = false)
			{
				ask contactRiver
				{
					diked <- true;
				}

			}

		}

		openSluice <- sluice where (each.open);
		ask openSluice
		{
			ask contactParcel
			{
				disableByDrain <- false;
				diked <- false;
			}

			ask contactRiver
			{
				diked <- false;
			}

		}

	}

	init
	{
		do load_land_use;
		do load_price;
		do load_investment;
		do load_cost;
		do load_transition;
		do load_suitability;
		do load_parcel;
		do load_river;
		do load_dike;
		do load_sluice;
	}

	reflex salinity_diffusion
	{
		ask parcels_not_diked
		{
			do diffusion;
		}

		ask parcels_not_diked
		{
			do update_salinity;
		}

		ask river
		{
			do diffusion;
		}

		ask river
		{
			do apply_diffusion;
		}

		ask river where (not (each.diked))
		{
			do salt_intrusion;
		}

	}

	reflex salt_intrusion
	{
		if (not rain_season)
		{
			the_main_river.river_salt_level <- 12.0;
			the_main_river.river_salt_level_tmp <- 12.0;
		} else
		{
			if (the_main_river.river_salt_level > 3)
			{
				the_main_river.river_salt_level <- the_main_river.river_salt_level - 1;
				the_main_river.river_salt_level_tmp <- the_main_river.river_salt_level - 1;
			}

		}

	}

	reflex end_simulation when: current_date.year = 2010 and current_date.month = 12
	{
		do pause;
	}

	reflex farmer_action when: every(# year)
	{
		ask farmer parallel: true
		{
			do make_decision;
		}

	}
	
}

species land_use
{
	string lu_code;
	map<string, float> transition_map;
	float risk;
	float average_yield_ha;
	float delay;
	map<int, float> price_map;
	map<int, float> cost_map;
	map<int, float> investment_map;
}

species suitability_case
{
	string soil_type;
	string acidity;
	int salinity;
	string lu_code;
	int suitability;
}

species parcel
{
	land_use my_land_use;
	map<int, string> lu_years;
	string soil_type;
	string acidity;
	map<int, float> salinity_years;
	float current_salinity max: 12.0;
	float current_salinity_tmp;
	bool diked <- false;
	bool disableByDrain <- false;
	list<parcel> neighborhood;
	list<parcel> availableNeighbors -> { [] + self + self.neighborhood where (not each.diked) };
	action diffusion
	{
		current_salinity_tmp <- current_salinity_tmp + min([12, mean((availableNeighbors) collect (each.current_salinity))]);
	}

	action update_salinity
	{
		current_salinity <- current_salinity_tmp;
		current_salinity_tmp <- 0.0;
	}

	aspect land_use
	{
		draw shape color: color_map[my_land_use.lu_code] border: # black;
	}

	aspect salinity
	{
		draw shape color: hsb(0.4 - 0.4 * (min([1.0, (max([0.0, current_salinity - min_salinity])) / max_salinity])), 1.0, 1.0);
	}

	aspect salinity2010
	{
		draw shape color: hsb(0.4 - 0.4 * (min([1.0, (max([0.0, salinity_years[2010] - min_salinity])) / max_salinity])), 1.0, 1.0);
	}

	aspect land_use2010
	{
		draw shape color: color_map[lu_years[2010]] border: # black;
	}

}

species river
{
	bool diked <- false;
	list<parcel> contactParcel <- parcel overlapping (self);
	list<parcel> availableParcel -> { contactParcel where (not each.diked) };
	list neighborhood <- river overlapping (self);
	list availableNeighbours -> { self.neighborhood where (not each.diked) };
	float river_salt_level <- 0.0;
	float river_salt_level_tmp <- 0.0;
	bool is_main_river <- false;
	action diffusion
	{
		if (is_main_river = false)
		{
			ask (availableNeighbours)
			{
				myself.river_salt_level_tmp <- myself.river_salt_level_tmp + river_salt_level;
			}

		}

	}

	action apply_diffusion
	{
		river_salt_level <- river_salt_level_tmp;
		river_salt_level_tmp <- river_salt_level;
	}

	action salt_intrusion
	{
		ask (availableParcel)
		{
			current_salinity_tmp <- current_salinity_tmp + myself.river_salt_level;
		}

	}

	aspect default
	{
		draw shape color: # blue;
	}

	aspect salinity
	{
		draw shape color: hsb(0.4 - 0.4 * (min([1.0, (max([0.0, self.river_salt_level - min_salinity])) / max_salinity])), 1.0, 1.0) border: # black;
	}

}

species dike
{
	list<parcel> contactParcel;
	list<river> contactRiver;
	aspect default
	{
		draw shape + 50 color: # green;
	}

}

species sluice
{
	bool open <- false;
	list<river> contactRiver <- river at_distance (50) where (not each.is_main_river);
	list<parcel> contactParcel <- parcel at_distance (50);
	list<dike> contactDike <- dike at_distance (50);
	aspect default
	{
		draw square(100) at: location color: open ? # green : # red;
	}

}

species farmer
{
	parcel my_parcel;
	list<farmer> neighborhood;
	float compute_expected_profit (land_use a_lu)
	{
		float price_of_product <- a_lu.price_map[current_date.year];
		return price_of_product / max_price;
	}

	float compute_risk (land_use a_lu)
	{
		return 1.0 - a_lu.risk;
	}

	float compute_implementation (land_use a_lu)
	{
		return ((3 - my_parcel.my_land_use.transition_map[a_lu.lu_code]) / 2);
	}

	float compute_suitability (land_use a_lu)
	{
		int f_salinity <- my_parcel.current_salinity <= 2 ? 2 : (my_parcel.current_salinity <= 4 ? 4 : (my_parcel.current_salinity <= 8 ? 8 : 12));
		suitability_case sc <- first(suitability_case where (each.lu_code = a_lu.lu_code and each.soil_type = my_parcel.soil_type and each.acidity = my_parcel.acidity and
		each.salinity = f_salinity));
		return 1.0 - (sc.suitability - 1) / 3.0;
	}

	float compute_neighborhood (land_use a_lu)
	{
		int nb_similars <- neighborhood count (each.my_parcel.my_land_use = a_lu);
		return nb_similars / length(neighborhood);
	}
	
	float compute_investment (land_use a_lu)
	{
		float investment_of_product <- a_lu.investment_map[current_date.year];
		return 1 - investment_of_product / max_investment;
	}

	float compute_cost (land_use a_lu)
	{
		float cost_of_product <- a_lu.cost_map[current_date.year];
		return 1 - cost_of_product / max_cost;
	}
	
	float compute_delay (land_use a_lu)
	{
		return 1 - a_lu.delay / max_delay;
	}

	list<list> land_use_eval (list<land_use> lus)
	{
		list<list> candidates;
		loop lu over: lus
		{
			list<float> cand;
			cand << compute_expected_profit(lu);
			cand << compute_risk(lu);
			cand << compute_implementation(lu);
			cand << compute_suitability(lu);
			cand << compute_neighborhood(lu);
			cand << compute_investment(lu);
			cand << compute_cost(lu);
			cand << compute_delay(lu);
			candidates << cand;
		}

		return candidates;
	}

	action make_decision
	{
		if (flip(probability_changing))
		{
			list<list> cands <- land_use_eval(list(land_use));
			int choice <- weighted_means_DM(cands, criteria);
			my_parcel.my_land_use <- land_use[choice];
		}

	}

}

experiment display_map
{
	output
	{
		display landuse background: # lightgray type: opengl
		{
			image file("../images/background.png") refresh: false;
			species parcel aspect: land_use;
			species river aspect: default;
			species sluice aspect: default;
			species dike aspect: default;
		}
		//		display salinity
		//		{
		//			species parcel aspect: salinity;
		//			species river aspect: default;
		//			species dike aspect: default;
		//			species sluice aspect: default;
		//		}
		//		display salinity2010
		//		{ 
		//			species parcel aspect: salinity2010;
		//			species river aspect: default;
		//			species dike aspect: default;
		//			species sluice aspect: default;
		//		}
		//		display salinity_river
		//		{ 
		//			species river aspect: salinity;
		//			species dike aspect: default;
		//			species sluice aspect: default;
		//		}
		display landuse2010 background: # lightgray type: opengl
		{
			image file("../images/background.png") refresh: false;
			species parcel aspect: land_use2010;
			species river aspect: default;
			species sluice aspect: default;
			species dike aspect: default;
		}

	}

}

experiment weight_exploration type: batch until: current_date.year = 2010 and current_date.month = 12 repeat: 1
{
	parameter "weight_investment" var: weight_investment min: 0.2 max: 0.8 step: 0.3;
	parameter "weight_cost" var: weight_cost min: 0.2 max: 0.8 step: 0.3;
	parameter "weight_delay" var: weight_delay min: 0.2 max: 0.8 step: 0.3;
	method exhaustive;
	
	init
	{
		save ["weight_investment", "weight_cost", "weight_delay", "rate_same_landuse_2010"] to: "resVariance.csv" type: "csv" rewrite: true header: false;
	}

	reflex saver
	{
		save [weight_investment, weight_cost, weight_delay, length(parcel) = 0 ? 0 : parcel count(each.my_land_use.lu_code = each.lu_years[2010]) / length(parcel) ] to: "resVariance.csv" type: "csv" rewrite: false;
	}

}

experiment lup_headless type: gui until: current_date.year = 2010 and current_date.month = 12
{
	parameter "weight_investment" var: weight_investment min: 0.2 max: 0.8 step: 0.3;
	parameter "weight_cost" var: weight_cost min: 0.2 max: 0.8 step: 0.3;
	parameter "weight_delay" var: weight_delay min: 0.2 max: 0.8 step: 0.3;
	output {
		monitor "rate_same_landuse_2010" value: length(parcel) = 0 ? 0 : parcel count(each.my_land_use.lu_code = each.lu_years[2010]) / length(parcel) ;	
	}

}