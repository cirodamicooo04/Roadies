package it.roadies.android_app.utils

import it.roadies.android_app.client.models.travel.TravelCreateRequest

object ContinentMapper {
    fun getContinent(countryCode: String?): TravelCreateRequest.Continent {
        return when (countryCode?.uppercase()) {
            "IT", "FR", "DE", "ES", "GB", "CH", "AT", "GR", "PT", "NL", "BE", "SE", "NO", "FI", "DK", "IE", "PL", "CZ", "RO", "HU", "BG", "SK", "HR", "RS", "SI", "EE", "LV", "LT", "IS", "UA" -> TravelCreateRequest.Continent.EUROPE
            "US", "CA", "MX", "BR", "AR", "CO", "CL", "PE", "CU", "VE", "EC", "GT", "HT", "BO", "DO", "HN", "PY", "NI", "SV", "CR", "PA", "UY", "JM", "PR" -> TravelCreateRequest.Continent.AMERICA
            "CN", "JP", "IN", "KR", "TH", "VN", "ID", "PH", "MY", "SG", "PK", "BD", "IR", "TR", "IL", "SA", "AE", "IQ", "SY", "JO", "LB", "OM", "KW", "QA", "BH", "YE", "LK", "MM", "NP", "AF", "UZ", "KZ" -> TravelCreateRequest.Continent.ASIA
            "AU", "NZ", "FJ", "PG", "SB", "VU", "WS", "KI", "TO", "FM", "PW", "MH" -> TravelCreateRequest.Continent.OCEANIA
            "ZA", "EG", "MA", "NG", "KE", "DZ", "TN", "ET", "TZ", "UG", "SD", "GH", "MZ", "AO", "CI", "CM", "MG", "ZW", "ZM", "SN", "ML", "BF", "TD", "GN", "RW", "BI", "BJ", "TG", "ER", "SL", "LR", "CF", "CG", "GA", "GQ", "DJ", "GM", "GW", "KM", "ST", "SC", "MU", "CV" -> TravelCreateRequest.Continent.AFRICA
            else -> TravelCreateRequest.Continent.EUROPE
        }
    }
}
