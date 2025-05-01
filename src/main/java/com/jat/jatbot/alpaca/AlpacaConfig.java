package com.jat.jatbot.alpaca;
import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.model.util.apitype.MarketDataWebsocketSourceType;
import net.jacobpeterson.alpaca.model.util.apitype.TraderAPIEndpointType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.jat.jatbot.InfoConfig;
import com.jat.jatbot.datahandlers.JATInfoHandler;

import okhttp3.OkHttpClient;

@Configuration
public class AlpacaConfig {
    @Primary
    @Bean
    public JATInfoHandler jatInfoHandler(InfoConfig infoConfig) {
        return new JATInfoHandler(infoConfig);
    }
    @Primary
    @Bean
    public String[] props(JATInfoHandler jatInfoHandler) {
        return jatInfoHandler.loadProperties();
    }

    @Primary
    @Bean
    public AlpacaAPI alpacaAPI(String[] props) {
        return new AlpacaAPI(
            props[0], props[1], 
            TraderAPIEndpointType.valueOf(props[2]),
            MarketDataWebsocketSourceType.valueOf(props[3]), 
            new OkHttpClient()
        );
    }
    @Bean
    public AlpacaController alpacaController(AlpacaAPI alpacaAPI, AlpacaStockHandler alpacaStockHandler, AlpacaCryptoHandler alpacaCryptoHandler, AlpacaAssetHandler alpacaAssetHandler, JATInfoHandler jatInfoHandler) {
        return new AlpacaController(alpacaAPI, alpacaStockHandler, alpacaCryptoHandler, alpacaAssetHandler, jatInfoHandler);
    }
    @Bean
    @Primary
    public net.jacobpeterson.alpaca.openapi.marketdata.ApiClient mdApiClient(AlpacaAPI alpacaAPI) {
        return alpacaAPI.marketData().getInternalAPIClient();
    }
    @Primary
    @Bean
    public net.jacobpeterson.alpaca.openapi.trader.ApiClient tApiClient(AlpacaAPI alpacaAPI) {
        return alpacaAPI.trader().getInternalAPIClient();
    }
    @Primary
    @Bean
    public AlpacaStockHandler alpacaStockHandler(net.jacobpeterson.alpaca.openapi.marketdata.ApiClient mdApiClient) {
        return new AlpacaStockHandler(mdApiClient);
    }
    @Primary
    @Bean
    public AlpacaCryptoHandler alpacaCryptoHandler(net.jacobpeterson.alpaca.openapi.marketdata.ApiClient mdApiClient) {
        return new AlpacaCryptoHandler(mdApiClient);
    }
    @Primary
    @Bean
    public AlpacaAssetHandler alpacaAssetHandler(net.jacobpeterson.alpaca.openapi.trader.ApiClient tApiClient) {
        return new AlpacaAssetHandler(tApiClient);
    }
}
