package com.darren.weather_app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate; // Import this!

import java.util.HashMap;
import java.util.Map;

@Controller
public class WeatherAppController {

    private Map<String, City> cityData = new HashMap<>();

    public WeatherAppController() {
        cityData.put("Fresno", new City("Fresno", 36.7378, -119.7871));
        cityData.put("Los Angeles", new City("Los Angeles", 34.0522, -118.2437));
        cityData.put("New York", new City("New York", 40.7128, -74.0060));
    }

    @GetMapping("/weather")
    public String viewHomePage(@RequestParam(name = "city", required = false) String cityName, Model model) {
        // Always pass the list of cities to the model so the dropdown stays populated
        model.addAttribute("cities", cityData.keySet());

        if (cityName != null) {
            City selectedCity = cityData.get(cityName);
            if (selectedCity != null) {
                // Call our new helper method
                getWeatherData(selectedCity, model);
            }
        }

        return "weather";
    }

    // --- NEW: Helper Method to call the API ---
    private void getWeatherData(City city, Model model) {
        String lat = String.valueOf(city.getLatitude());
        String lon = String.valueOf(city.getLongitude());

        // 1. Build the URL
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                "&longitude=" + lon +
                "&current=temperature_2m,wind_speed_10m";

        // 2. Make the HTTP Request
        RestTemplate restTemplate = new RestTemplate();
        // We ask Spring to parse the JSON response into a generic Map
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        // 3. Parse the JSON (The structure is: Root -> "current" -> "temperature_2m")
        if (response != null && response.containsKey("current")) {
            // "current" is actually another nested Map inside the response
            Map<String, Object> currentData = (Map<String, Object>) response.get("current");

            Object temp = currentData.get("temperature_2m");
            Object wind = currentData.get("wind_speed_10m");

            // 4. Put the data into the "Suitcase" (Model) for the HTML page
            model.addAttribute("temp", temp);
            model.addAttribute("wind", wind);
            model.addAttribute("selectedCity", city.getName());
        }
    }
}
