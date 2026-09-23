package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * Official Asset Directory Icon Component for CryptoCycles.
 * Loads the current CoinGecko brand image for each of the 100 catalog coins.
 */
@Composable
fun CryptoIconGraphic(
    symbol: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val sym = symbol.uppercase().trim()
    val cleanSym = sym.lowercase()
    val context = LocalContext.current

    // CoinGecko & CryptoIcons CDN endpoint resolver
    val primaryUrl = remember(cleanSym) {
        getCoinIconUrl(cleanSym)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF0F1218))
            .border(1.dp, Color(0xFF22262F), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(primaryUrl)
                .crossfade(true)
                .build(),
            contentDescription = "$symbol icon",
            modifier = Modifier.fillMaxSize(),
            loading = {
                CryptoVectorFallback(sym = sym, size = size)
            },
            error = {
                CryptoVectorFallback(sym = sym, size = size)
            }
        )
    }
}

internal fun getCoinIconUrl(sym: String): String {
    return when (sym) {
        "aave" -> "https://coin-images.coingecko.com/coins/images/12645/large/aave-token-round.png"
        "ada" -> "https://coin-images.coingecko.com/coins/images/975/large/cardano.png"
        "aero" -> "https://coin-images.coingecko.com/coins/images/31745/large/token.png"
        "akt" -> "https://coin-images.coingecko.com/coins/images/12785/large/akash-logo.png"
        "algo" -> "https://coin-images.coingecko.com/coins/images/4380/large/download.png"
        "apt" -> "https://coin-images.coingecko.com/coins/images/26455/large/Aptos-Network-Profile-Picture_%281%29.png"
        "ar" -> "https://coin-images.coingecko.com/coins/images/4343/large/oRt6SiEN_400x400.jpg"
        "arb" -> "https://coin-images.coingecko.com/coins/images/16547/large/arb.jpg"
        "aster" -> "https://coin-images.coingecko.com/coins/images/69040/large/_ASTER.png"
        "atom" -> "https://coin-images.coingecko.com/coins/images/1481/large/cosmos_hub.png"
        "avax" -> "https://coin-images.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png"
        "axs" -> "https://coin-images.coingecko.com/coins/images/13029/large/axie_infinity_logo.png"
        "bch" -> "https://coin-images.coingecko.com/coins/images/780/large/bitcoin-cash-circle.png"
        "bnb" -> "https://coin-images.coingecko.com/coins/images/825/large/bnb-icon2_2x.png"
        "bonk" -> "https://coin-images.coingecko.com/coins/images/28600/large/bonk.jpg"
        "btc" -> "https://coin-images.coingecko.com/coins/images/1/large/bitcoin.png"
        "cake" -> "https://coin-images.coingecko.com/coins/images/12632/large/pancakeswap-cake-logo_%281%29.png"
        "chz" -> "https://coin-images.coingecko.com/coins/images/8834/large/CHZ_Token_updated.png"
        "comp" -> "https://coin-images.coingecko.com/coins/images/10775/large/COMP.png"
        "crv" -> "https://coin-images.coingecko.com/coins/images/12124/large/Curve.png"
        "cvx" -> "https://coin-images.coingecko.com/coins/images/15585/large/convex.png"
        "dash" -> "https://coin-images.coingecko.com/coins/images/19/large/dash-logo.png"
        "doge" -> "https://coin-images.coingecko.com/coins/images/5/large/dogecoin.png"
        "dot" -> "https://coin-images.coingecko.com/coins/images/12171/large/polkadot.jpg"
        "dydx" -> "https://coin-images.coingecko.com/coins/images/32594/large/dydx.png"
        "eigen" -> "https://coin-images.coingecko.com/coins/images/37441/large/eigencloud.jpg"
        "ena" -> "https://coin-images.coingecko.com/coins/images/36530/large/ethena.png"
        "ens" -> "https://coin-images.coingecko.com/coins/images/19785/large/ENS.jpg"
        "etc" -> "https://coin-images.coingecko.com/coins/images/453/large/ethereum-classic-logo.png"
        "eth" -> "https://coin-images.coingecko.com/coins/images/279/large/ethereum.png"
        "ethfi" -> "https://coin-images.coingecko.com/coins/images/35958/large/etherfi.jpeg"
        "fet", "asi" -> "https://coin-images.coingecko.com/coins/images/5681/large/ASI.png"
        "fil" -> "https://coin-images.coingecko.com/coins/images/12817/large/filecoin.png"
        "floki" -> "https://coin-images.coingecko.com/coins/images/16746/large/PNG_image.png"
        "gala" -> "https://coin-images.coingecko.com/coins/images/12493/large/GALA_token_image_-_200PNG.png"
        "glm" -> "https://coin-images.coingecko.com/coins/images/542/large/Golem_Submark_Positive_RGB.png"
        "gmx" -> "https://coin-images.coingecko.com/coins/images/18323/large/arbit.png"
        "grt" -> "https://coin-images.coingecko.com/coins/images/13397/large/Graph_Token.png"
        "hbar" -> "https://coin-images.coingecko.com/coins/images/3688/large/hbar.png"
        "hype" -> "https://coin-images.coingecko.com/coins/images/50882/large/hyperliquid.jpg"
        "icp" -> "https://coin-images.coingecko.com/coins/images/14495/large/Internet_Computer_logo.png"
        "imx" -> "https://coin-images.coingecko.com/coins/images/17233/large/immutableX-symbol-BLK-RGB.png"
        "inj" -> "https://coin-images.coingecko.com/coins/images/12882/large/Other_200x200.png"
        "iota" -> "https://coin-images.coingecko.com/coins/images/692/large/IOTA_Thumbnail_%281%29.png"
        "jasmy" -> "https://coin-images.coingecko.com/coins/images/13876/large/JASMY200x200.jpg"
        "jst" -> "https://coin-images.coingecko.com/coins/images/11095/large/JUST.jpg"
        "jup" -> "https://coin-images.coingecko.com/coins/images/34188/large/jup.png"
        "kas" -> "https://coin-images.coingecko.com/coins/images/25751/large/kaspa-icon-exchanges.png"
        "ldo" -> "https://coin-images.coingecko.com/coins/images/13573/large/Lido_DAO.png"
        "link" -> "https://coin-images.coingecko.com/coins/images/877/large/Chainlink_Logo_500.png"
        "lit" -> "https://coin-images.coingecko.com/coins/images/71121/large/lighter.png"
        "ltc" -> "https://coin-images.coingecko.com/coins/images/2/large/litecoin.png"
        "mana" -> "https://coin-images.coingecko.com/coins/images/878/large/decentraland-mana.png"
        "mina" -> "https://coin-images.coingecko.com/coins/images/15628/large/JM4_vQ34_400x400.png"
        "morpho" -> "https://coin-images.coingecko.com/coins/images/29837/large/Morpho-token-icon.png"
        "near" -> "https://coin-images.coingecko.com/coins/images/10365/large/near.jpg"
        "night" -> "https://coin-images.coingecko.com/coins/images/71015/large/midnight.png"
        "ondo" -> "https://coin-images.coingecko.com/coins/images/26580/large/ONDO.png"
        "op" -> "https://coin-images.coingecko.com/coins/images/25244/large/Token.png"
        "paxg" -> "https://coin-images.coingecko.com/coins/images/9519/large/asset-paxg.png"
        "pendle" -> "https://coin-images.coingecko.com/coins/images/15069/large/Pendle_Logo_Normal-03.png"
        "pengu" -> "https://coin-images.coingecko.com/coins/images/52622/large/PUDGY_PENGUINS_PENGU_PFP.png"
        "pepe" -> "https://coin-images.coingecko.com/coins/images/29850/large/pepe-token.jpeg"
        "pol", "matic" -> "https://coin-images.coingecko.com/coins/images/32440/large/pol.png"
        "pump" -> "https://coin-images.coingecko.com/coins/images/67164/large/pump.jpg"
        "pyth" -> "https://coin-images.coingecko.com/coins/images/31924/large/pyth.png"
        "qnt" -> "https://coin-images.coingecko.com/coins/images/3370/large/5ZOu7brX_400x400.jpg"
        "ray" -> "https://coin-images.coingecko.com/coins/images/13928/large/PSigc4ie_400x400.jpg"
        "render", "rndr" -> "https://coin-images.coingecko.com/coins/images/11636/large/rndr.png"
        "rune" -> "https://coin-images.coingecko.com/coins/images/6595/large/THORChain_RUNE_Token.png"
        "s" -> "https://coin-images.coingecko.com/coins/images/38108/large/200x200_Sonic_Logo.png"
        "sand" -> "https://coin-images.coingecko.com/coins/images/12129/large/sandbox_logo.jpg"
        "sei" -> "https://coin-images.coingecko.com/coins/images/28205/large/Sei_Logo_-_Transparent.png"
        "shib" -> "https://coin-images.coingecko.com/coins/images/11939/large/shiba.png"
        "sky" -> "https://coin-images.coingecko.com/coins/images/39925/large/sky.jpg"
        "snx" -> "https://coin-images.coingecko.com/coins/images/3406/large/SNX.png"
        "sol" -> "https://coin-images.coingecko.com/coins/images/4128/large/solana.png"
        "spx" -> "https://coin-images.coingecko.com/coins/images/31401/large/centeredcoin_%281%29.png"
        "strk" -> "https://coin-images.coingecko.com/coins/images/26433/large/starknet.png"
        "stx" -> "https://coin-images.coingecko.com/coins/images/2069/large/Stacks_Logo_png.png"
        "sui" -> "https://coin-images.coingecko.com/coins/images/26375/large/sui-ocean-square.png"
        "tao" -> "https://coin-images.coingecko.com/coins/images/28452/large/ARUsPeNQ_400x400.jpeg"
        "theta" -> "https://coin-images.coingecko.com/coins/images/2538/large/theta-token-logo.png"
        "tia" -> "https://coin-images.coingecko.com/coins/images/31967/large/tia.jpg"
        "trump" -> "https://coin-images.coingecko.com/coins/images/53746/large/trump.png"
        "trx" -> "https://coin-images.coingecko.com/coins/images/1094/large/photo_2026-04-13_09-59-16.png"
        "uni" -> "https://coin-images.coingecko.com/coins/images/12504/large/uniswap-logo.png"
        "vet" -> "https://coin-images.coingecko.com/coins/images/1167/large/VET.png"
        "virtual" -> "https://coin-images.coingecko.com/coins/images/34057/large/LOGOMARK.png"
        "w" -> "https://coin-images.coingecko.com/coins/images/35087/large/W_Token_%283%29.png"
        "wif" -> "https://coin-images.coingecko.com/coins/images/33566/large/dogwifhat.jpg"
        "wld" -> "https://coin-images.coingecko.com/coins/images/31069/large/worldcoin.jpeg"
        "wlfi" -> "https://coin-images.coingecko.com/coins/images/50767/large/wlfi.png"
        "xlm" -> "https://coin-images.coingecko.com/coins/images/100/large/fmpFRHHQ_400x400.jpg"
        "xmr" -> "https://coin-images.coingecko.com/coins/images/69/large/monero_logo.png"
        "xrp" -> "https://coin-images.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png"
        "xtz" -> "https://coin-images.coingecko.com/coins/images/976/large/Tezos-logo.png"
        "zec" -> "https://coin-images.coingecko.com/coins/images/486/large/Brandmark-Yellow_%281%29.png"
        "zk" -> "https://coin-images.coingecko.com/coins/images/38043/large/ZKTokenBlack.png"
        "zro" -> "https://coin-images.coingecko.com/coins/images/28206/large/ftxG9_TJ_400x400.jpeg"

        // Extra symbols that can appear in live prints / alerts.
        "flr" -> "https://coin-images.coingecko.com/coins/images/28624/large/FLR-1000x1000-black.png"
        "ftm" -> "https://coin-images.coingecko.com/coins/images/4001/large/Fantom_round.png"
        "ton" -> "https://coin-images.coingecko.com/coins/images/17980/large/ton_symbol.png"
        "rose" -> "https://coin-images.coingecko.com/coins/images/13162/large/rose.png"
        "kava" -> "https://coin-images.coingecko.com/coins/images/9761/large/kava.png"
        "core" -> "https://coin-images.coingecko.com/coins/images/28931/large/core_dao.jpeg"
        "blast" -> "https://coin-images.coingecko.com/coins/images/35515/large/blast.png"
        "zeta" -> "https://coin-images.coingecko.com/coins/images/29729/large/zeta.png"
        "mnt" -> "https://coin-images.coingecko.com/coins/images/30980/large/Mantle.png"
        "metis" -> "https://coin-images.coingecko.com/coins/images/15595/large/metis.PNG"
        "manta" -> "https://coin-images.coingecko.com/coins/images/34289/large/manta.png"
        "taiko" -> "https://coin-images.coingecko.com/coins/images/37730/large/taiko.png"
        "ocean" -> "https://coin-images.coingecko.com/coins/images/3687/large/ocean-protocol-logo.png"
        "io" -> "https://coin-images.coingecko.com/coins/images/36737/large/io.png"
        "ath" -> "https://coin-images.coingecko.com/coins/images/38479/large/aethir.png"
        "mkr" -> "https://coin-images.coingecko.com/coins/images/1364/large/Mark_Maker.png"
        "frax" -> "https://coin-images.coingecko.com/coins/images/13422/large/frax_share.png"
        "rpl" -> "https://coin-images.coingecko.com/coins/images/2090/large/rocket_pool_logo.png"
        "fxs" -> "https://coin-images.coingecko.com/coins/images/13422/large/frax_share.png"
        "brett" -> "https://coin-images.coingecko.com/coins/images/35529/large/brett.png"
        "popcat" -> "https://coin-images.coingecko.com/coins/images/33760/large/popcat.png"
        "neiro" -> "https://coin-images.coingecko.com/coins/images/39556/large/neiro.png"
        "dogs" -> "https://coin-images.coingecko.com/coins/images/39697/large/dogs.png"
        "mog" -> "https://coin-images.coingecko.com/coins/images/31059/large/mog.png"
        "turbo" -> "https://coin-images.coingecko.com/coins/images/30116/large/turbo.png"
        "zil" -> "https://coin-images.coingecko.com/coins/images/2687/large/Zilliqa-logo.png"
        "blur" -> "https://coin-images.coingecko.com/coins/images/28453/large/blur.png"
        else -> "https://cdn.jsdelivr.net/gh/atomiclabs/cryptocurrency-icons@1a63539be1e3747b1af46612722ab0f5a0020173/128/color/$sym.png"
    }
}


@Composable
private fun CryptoVectorFallback(
    sym: String,
    size: Dp
) {
    val hasDedicatedVector = when (sym) {
        "SOL", "SOLANA", "ETH", "ETHEREUM", "XRP", "RIPPLE", "LINK", "CHAINLINK",
        "BTC", "BITCOIN", "X", "XMONEY", "UTK", "ADA", "CARDANO", "AVAX", "AVALANCHE",
        "DOGE", "DOGECOIN", "BNB", "BINANCE", "SUI", "NEAR", "DOT", "POLKADOT",
        "MATIC", "POL", "POLYGON", "TON", "TONCOIN", "TRX", "TRON", "UNI", "UNISWAP",
        "PEPE", "SHIB", "SHIBA", "ATOM", "COSMOS", "ARB", "ARBITRUM", "OP", "OPTIMISM",
        "INJ", "INJECTIVE", "TIA", "CELESTIA", "RENDER", "RNDR", "FET", "ASI", "FETCH",
        "TAO", "BITTENSOR", "KAS", "KASPA", "HBAR", "HEDERA", "XLM", "STELLAR",
        "ALGO", "ALGORAND", "ICP", "FIL", "FILECOIN", "AAVE", "MKR", "MAKER",
        "STX", "STACKS", "PYTH", "JUP", "JUPITER", "WIF", "BONK", "POPCAT",
        "ENA", "ETHENA", "EIGEN", "EIGENLAYER", "MNT", "MANTLE", "STRK", "STARKNET",
        "ZK", "ZKSYNC", "ZRO", "LAYERZERO", "W", "WORMHOLE", "CRV", "CURVE",
        "AKT", "AKASH", "AR", "ARWEAVE", "HNT", "HELIUM", "ONDO", "PENDLE",
        "SEI", "WLD", "WORLDCOIN", "APT", "APTOS", "FTM", "SONIC", "FANTOM", "S",
        "XMR", "MONERO", "GRT", "GRAPH", "FLR", "FLARE", "ROSE", "MINA", "XTZ",
        "KAVA", "CORE", "BLAST", "ZETA", "IMX", "METIS", "MANTA", "TAIKO",
        "THETA", "OCEAN", "GLM", "IO", "ATH", "LDO", "SNX", "DYDX", "GMX",
        "RUNE", "RAY", "AERO", "MORPHO", "COMP", "CVX", "RPL", "FXS", "CAKE",
        "FLOKI", "BRETT", "NEIRO", "DOGS", "MOG", "TURBO", "JASMY", "IOTA",
        "GALA", "AXS", "SAND", "MANA", "ZIL", "BLUR", "CHZ" -> true
        else -> false
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            when (sym) {
                // Tier 1 Major
                "SOL", "SOLANA" -> drawSolanaLogo(w, h)
                "ETH", "ETHEREUM" -> drawEthereumLogo(cx, cy, w, h)
                "XRP", "RIPPLE" -> drawXrpLogo(cx, cy, w, h)
                "LINK", "CHAINLINK" -> drawChainlinkLogo(cx, cy, w, h)
                "BTC", "BITCOIN" -> drawBitcoinLogo(cx, cy, w, h)
                "X", "XMONEY", "UTK" -> drawXLogo(cx, cy, w, h)
                "ADA", "CARDANO" -> drawCardanoLogo(cx, cy, w, h)
                "AVAX", "AVALANCHE" -> drawAvalancheLogo(cx, cy, w, h)
                "DOGE", "DOGECOIN" -> drawDogecoinLogo(cx, cy, w, h)
                "BNB", "BINANCE" -> drawBnbLogo(cx, cy, w, h)
                "SUI" -> drawSuiLogo(cx, cy, w, h)
                "FLR", "FLARE" -> drawFlareLogo(cx, cy, w, h)
                "NEAR" -> drawNearLogo(cx, cy, w, h)
                "DOT", "POLKADOT" -> drawPolkadotLogo(cx, cy, w, h)
                "MATIC", "POL", "POLYGON" -> drawPolygonLogo(cx, cy, w, h)
                "TON", "TONCOIN" -> drawTonLogo(cx, cy, w, h)
                "TRX", "TRON" -> drawTronLogo(cx, cy, w, h)
                "UNI", "UNISWAP" -> drawUniswapLogo(cx, cy, w, h)
                "PEPE" -> drawPepeLogo(cx, cy, w, h)
                "SHIB", "SHIBA" -> drawShibaLogo(cx, cy, w, h)
                "ATOM", "COSMOS" -> drawCosmosLogo(cx, cy, w, h)
                "ARB", "ARBITRUM" -> drawArbitrumLogo(cx, cy, w, h)
                "OP", "OPTIMISM" -> drawOptimismLogo(cx, cy, w, h)
                "INJ", "INJECTIVE" -> drawInjectiveLogo(cx, cy, w, h)
                "TIA", "CELESTIA" -> drawCelestiaLogo(cx, cy, w, h)
                "RENDER", "RNDR" -> drawRenderLogo(cx, cy, w, h)
                "FET", "ASI", "FETCH" -> drawFetchLogo(cx, cy, w, h)
                "TAO", "BITTENSOR" -> drawTaoLogo(cx, cy, w, h)
                "KAS", "KASPA" -> drawKaspaLogo(cx, cy, w, h)
                "HBAR", "HEDERA" -> drawHederaLogo(cx, cy, w, h)
                "XLM", "STELLAR" -> drawStellarLogo(cx, cy, w, h)
                "ALGO", "ALGORAND" -> drawAlgorandLogo(cx, cy, w, h)
                "ICP" -> drawIcpLogo(cx, cy, w, h)
                "FIL", "FILECOIN" -> drawFilecoinLogo(cx, cy, w, h)
                "AAVE" -> drawAaveLogo(cx, cy, w, h)
                "MKR", "MAKER", "SKY" -> drawMakerLogo(cx, cy, w, h)
                "STX", "STACKS" -> drawStacksLogo(cx, cy, w, h)
                "PYTH" -> drawPythLogo(cx, cy, w, h)
                "JUP", "JUPITER" -> drawJupiterLogo(cx, cy, w, h)
                "WIF" -> drawWifLogo(cx, cy, w, h)
                "BONK" -> drawBonkLogo(cx, cy, w, h)
                "POPCAT" -> drawPopcatLogo(cx, cy, w, h)
                "ENA", "ETHENA" -> drawEthenaLogo(cx, cy, w, h)
                "EIGEN", "EIGENLAYER" -> drawEigenLayerLogo(cx, cy, w, h)
                "MNT", "MANTLE" -> drawMantleLogo(cx, cy, w, h)
                "STRK", "STARKNET" -> drawStarknetLogo(cx, cy, w, h)
                "ZK", "ZKSYNC" -> drawZkSyncLogo(cx, cy, w, h)
                "ZRO", "LAYERZERO" -> drawLayerZeroLogo(cx, cy, w, h)
                "W", "WORMHOLE" -> drawWormholeLogo(cx, cy, w, h)
                "CRV", "CURVE" -> drawCurveLogo(cx, cy, w, h)
                "AKT", "AKASH" -> drawAkashLogo(cx, cy, w, h)
                "AR", "ARWEAVE" -> drawArweaveLogo(cx, cy, w, h)
                "HNT", "HELIUM" -> drawHeliumLogo(cx, cy, w, h)
                "ONDO" -> drawOndoLogo(cx, cy, w, h)
                "PENDLE" -> drawPendleLogo(cx, cy, w, h)
                "SEI" -> drawSeiLogo(cx, cy, w, h)
                "WLD", "WORLDCOIN" -> drawWorldcoinLogo(cx, cy, w, h)
                "APT", "APTOS" -> drawAptosLogo(cx, cy, w, h)
                "FTM", "SONIC", "FANTOM", "S" -> drawFantomLogo(cx, cy, w, h)
                "XMR", "MONERO" -> drawMoneroLogo(cx, cy, w, h)
                "GRT", "GRAPH" -> drawGraphLogo(cx, cy, w, h)
                "ROSE" -> drawRoseLogo(cx, cy, w, h)
                "MINA" -> drawMinaLogo(cx, cy, w, h)
                "XTZ" -> drawTezosLogo(cx, cy, w, h)
                "KAVA" -> drawKavaLogo(cx, cy, w, h)
                "CORE" -> drawCoreLogo(cx, cy, w, h)
                "BLAST" -> drawBlastLogo(cx, cy, w, h)
                "ZETA" -> drawZetaLogo(cx, cy, w, h)
                "IMX" -> drawImxLogo(cx, cy, w, h)
                "METIS" -> drawMetisLogo(cx, cy, w, h)
                "MANTA" -> drawMantaLogo(cx, cy, w, h)
                "TAIKO" -> drawTaikoLogo(cx, cy, w, h)
                "THETA" -> drawThetaLogo(cx, cy, w, h)
                "OCEAN" -> drawOceanLogo(cx, cy, w, h)
                "GLM" -> drawGlmLogo(cx, cy, w, h)
                "IO" -> drawIoLogo(cx, cy, w, h)
                "ATH" -> drawAthLogo(cx, cy, w, h)
                "LDO" -> drawLdoLogo(cx, cy, w, h)
                "SNX" -> drawSnxLogo(cx, cy, w, h)
                "DYDX" -> drawDydxLogo(cx, cy, w, h)
                "GMX" -> drawGmxLogo(cx, cy, w, h)
                "RUNE" -> drawRuneLogo(cx, cy, w, h)
                "RAY" -> drawRayLogo(cx, cy, w, h)
                "AERO" -> drawAeroLogo(cx, cy, w, h)
                "MORPHO" -> drawMorphoLogo(cx, cy, w, h)
                "COMP" -> drawCompLogo(cx, cy, w, h)
                "CVX" -> drawCvxLogo(cx, cy, w, h)
                "RPL" -> drawRplLogo(cx, cy, w, h)
                "FXS", "FRAX" -> drawFxsLogo(cx, cy, w, h)
                "CAKE" -> drawCakeLogo(cx, cy, w, h)
                "FLOKI" -> drawFlokiLogo(cx, cy, w, h)
                "BRETT" -> drawBrettLogo(cx, cy, w, h)
                "NEIRO" -> drawNeiroLogo(cx, cy, w, h)
                "DOGS" -> drawDogsLogo(cx, cy, w, h)
                "MOG" -> drawMogLogo(cx, cy, w, h)
                "TURBO" -> drawTurboLogo(cx, cy, w, h)
                "JASMY" -> drawJasmyLogo(cx, cy, w, h)
                "IOTA" -> drawIotaLogo(cx, cy, w, h)
                "GALA" -> drawGalaLogo(cx, cy, w, h)
                "AXS" -> drawAxsLogo(cx, cy, w, h)
                "SAND" -> drawSandLogo(cx, cy, w, h)
                "MANA" -> drawManaLogo(cx, cy, w, h)
                "ZIL" -> drawZilLogo(cx, cy, w, h)
                "BLUR" -> drawBlurLogo(cx, cy, w, h)
                "CHZ" -> drawChzLogo(cx, cy, w, h)

                else -> drawGenericCryptoLogo(sym, cx, cy, w, h)
            }
        }

        if (!hasDedicatedVector) {
            Text(
                text = sym.take(3),
                color = Color.White,
                fontSize = if (size > 36.dp) 12.sp else 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ----------------------------------------------------
// 1. SOLANA LOGO (3 skewed horizontal gradient bars)
// ----------------------------------------------------
private fun DrawScope.drawSolanaLogo(w: Float, h: Float) {
    val barHeight = h * 0.12f
    val barWidth = w * 0.54f
    val skewOffset = w * 0.12f
    val startX = (w - barWidth) / 2f

    val solanaGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF00FFA3), Color(0xFFDC1FFF)),
        startX = startX - skewOffset,
        endX = startX + barWidth + skewOffset
    )

    val topPath = Path().apply {
        val y = h * 0.28f
        moveTo(startX + skewOffset, y)
        lineTo(startX + barWidth, y)
        lineTo(startX + barWidth - skewOffset, y + barHeight)
        lineTo(startX, y + barHeight)
        close()
    }
    drawPath(topPath, solanaGradient)

    val midPath = Path().apply {
        val y = h * 0.44f
        moveTo(startX, y)
        lineTo(startX + barWidth - skewOffset, y)
        lineTo(startX + barWidth, y + barHeight)
        lineTo(startX + skewOffset, y + barHeight)
        close()
    }
    drawPath(midPath, solanaGradient)

    val botPath = Path().apply {
        val y = h * 0.60f
        moveTo(startX + skewOffset, y)
        lineTo(startX + barWidth, y)
        lineTo(startX + barWidth - skewOffset, y + barHeight)
        lineTo(startX, y + barHeight)
        close()
    }
    drawPath(botPath, solanaGradient)
}

// ----------------------------------------------------
// 2. ETHEREUM LOGO (Faceted 3D Diamond)
// ----------------------------------------------------
private fun DrawScope.drawEthereumLogo(cx: Float, cy: Float, w: Float, h: Float) {
    val scale = h * 0.28f

    val topLeft = Path().apply {
        moveTo(cx, cy - scale * 1.35f)
        lineTo(cx - scale * 0.72f, cy - scale * 0.05f)
        lineTo(cx, cy + scale * 0.35f)
        close()
    }
    drawPath(topLeft, Color(0xFFE8E8E8))

    val topRight = Path().apply {
        moveTo(cx, cy - scale * 1.35f)
        lineTo(cx + scale * 0.72f, cy - scale * 0.05f)
        lineTo(cx, cy + scale * 0.35f)
        close()
    }
    drawPath(topRight, Color(0xFF9E9E9E))

    val botLeft = Path().apply {
        moveTo(cx, cy + scale * 0.52f)
        lineTo(cx - scale * 0.72f, cy + scale * 0.12f)
        lineTo(cx, cy + scale * 1.35f)
        close()
    }
    drawPath(botLeft, Color(0xFFD4D4D4))

    val botRight = Path().apply {
        moveTo(cx, cy + scale * 0.52f)
        lineTo(cx + scale * 0.72f, cy + scale * 0.12f)
        lineTo(cx, cy + scale * 1.35f)
        close()
    }
    drawPath(botRight, Color(0xFF888888))
}

// ----------------------------------------------------
// 3. XRP LOGO
// ----------------------------------------------------
private fun DrawScope.drawXrpLogo(cx: Float, cy: Float, w: Float, h: Float) {
    val strokeWidth = w * 0.08f
    val color = Color(0xFFFFFFFF)

    val topCurve = Path().apply {
        moveTo(cx - w * 0.26f, cy - h * 0.24f)
        cubicTo(
            cx - w * 0.12f, cy - h * 0.24f,
            cx - w * 0.06f, cy - h * 0.04f,
            cx, cy - h * 0.04f
        )
        cubicTo(
            cx + w * 0.06f, cy - h * 0.04f,
            cx + w * 0.12f, cy - h * 0.24f,
            cx + w * 0.26f, cy - h * 0.24f
        )
    }
    drawPath(topCurve, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

    val botCurve = Path().apply {
        moveTo(cx - w * 0.26f, cy + h * 0.24f)
        cubicTo(
            cx - w * 0.12f, cy + h * 0.24f,
            cx - w * 0.06f, cy + h * 0.04f,
            cx, cy + h * 0.04f
        )
        cubicTo(
            cx + w * 0.06f, cy + h * 0.04f,
            cx + w * 0.12f, cy + h * 0.24f,
            cx + w * 0.26f, cy + h * 0.24f
        )
    }
    drawPath(botCurve, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

// ----------------------------------------------------
// 4. CHAINLINK LOGO (Hexagon)
// ----------------------------------------------------
private fun DrawScope.drawChainlinkLogo(cx: Float, cy: Float, w: Float, h: Float) {
    val outerR = w * 0.32f
    val strokeW = w * 0.09f
    val hexPath = Path()
    for (i in 0..5) {
        val angle = Math.toRadians((i * 60 - 30).toDouble())
        val x = (cx + outerR * Math.cos(angle)).toFloat()
        val y = (cy + outerR * Math.sin(angle)).toFloat()
        if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
    }
    hexPath.close()

    drawPath(
        path = hexPath,
        color = Color(0xFFFFFFFF),
        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

// ----------------------------------------------------
// 5. BITCOIN LOGO (Gold circle & ₿ sign)
// ----------------------------------------------------
private fun DrawScope.drawBitcoinLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(color = Color(0xFFF7931A), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    val bColor = Color.White
    val top = cy - h * 0.22f
    val bot = cy + h * 0.22f
    val xBar = cx - w * 0.08f

    drawLine(bColor, Offset(xBar, top), Offset(xBar, bot), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(bColor, Offset(cx - w * 0.03f, top - h * 0.06f), Offset(cx - w * 0.03f, top), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
    drawLine(bColor, Offset(cx + w * 0.05f, top - h * 0.06f), Offset(cx + w * 0.05f, top), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
    drawLine(bColor, Offset(cx - w * 0.03f, bot), Offset(cx - w * 0.03f, bot + h * 0.06f), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
    drawLine(bColor, Offset(cx + w * 0.05f, bot), Offset(cx + w * 0.05f, bot + h * 0.06f), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)

    val topBulge = Path().apply {
        moveTo(xBar, top)
        cubicTo(cx + w * 0.16f, top, cx + w * 0.16f, cy - h * 0.01f, xBar, cy - h * 0.01f)
    }
    drawPath(topBulge, bColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))

    val botBulge = Path().apply {
        moveTo(xBar, cy - h * 0.01f)
        cubicTo(cx + w * 0.19f, cy - h * 0.01f, cx + w * 0.19f, bot, xBar, bot)
    }
    drawPath(botBulge, bColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))
}

// ----------------------------------------------------
// 6. X / XMONEY LOGO
// ----------------------------------------------------
private fun DrawScope.drawXLogo(cx: Float, cy: Float, w: Float, h: Float) {
    val r = w * 0.22f
    val strokeW = w * 0.09f
    val color = Color.White
    drawLine(color = color, start = Offset(cx - r, cy - r), end = Offset(cx + r, cy + r), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(color = color, start = Offset(cx + r, cy - r), end = Offset(cx - r, cy + r), strokeWidth = strokeW, cap = StrokeCap.Round)
}

// ----------------------------------------------------
// 7. CARDANO LOGO
// ----------------------------------------------------
private fun DrawScope.drawCardanoLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0033AD), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.08f, center = Offset(cx, cy))
    val rings = 6
    val ringR = w * 0.24f
    for (i in 0 until rings) {
        val angle = Math.toRadians((i * 60).toDouble())
        val x = (cx + ringR * Math.cos(angle)).toFloat()
        val y = (cy + ringR * Math.sin(angle)).toFloat()
        drawCircle(Color.White, radius = w * 0.045f, center = Offset(x, y))
    }
}

// ----------------------------------------------------
// 8. AVALANCHE LOGO
// ----------------------------------------------------
private fun DrawScope.drawAvalancheLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFE84142), radius = w * 0.38f, center = Offset(cx, cy))
    val r = w * 0.22f
    val path = Path().apply {
        moveTo(cx, cy - r)
        lineTo(cx + r, cy + r)
        lineTo(cx + r * 0.45f, cy + r)
        lineTo(cx, cy - r * 0.1f)
        lineTo(cx - r * 0.45f, cy + r)
        lineTo(cx - r, cy + r)
        close()
    }
    drawPath(path, Color.White)
}

// ----------------------------------------------------
// 9. DOGECOIN LOGO
// ----------------------------------------------------
private fun DrawScope.drawDogecoinLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFC2A633), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    val bColor = Color.White
    val top = cy - h * 0.2f
    val bot = cy + h * 0.2f
    val xBar = cx - w * 0.08f

    drawLine(bColor, Offset(xBar, top), Offset(xBar, bot), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(bColor, Offset(xBar - w * 0.07f, cy), Offset(xBar + w * 0.08f, cy), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
    val dPath = Path().apply {
        moveTo(xBar, top)
        cubicTo(cx + w * 0.2f, top, cx + w * 0.2f, bot, xBar, bot)
    }
    drawPath(dPath, bColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))
}

// ----------------------------------------------------
// 10. BNB LOGO
// ----------------------------------------------------
private fun DrawScope.drawBnbLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFF3BA2F), radius = w * 0.38f, center = Offset(cx, cy))
    val bnbColor = Color(0xFF0F1218)
    val dSize = w * 0.09f
    drawDiamond(cx, cy, dSize, bnbColor)
    val offset = w * 0.16f
    drawDiamond(cx, cy - offset, dSize * 0.8f, bnbColor)
    drawDiamond(cx, cy + offset, dSize * 0.8f, bnbColor)
    drawDiamond(cx - offset, cy, dSize * 0.8f, bnbColor)
    drawDiamond(cx + offset, cy, dSize * 0.8f, bnbColor)
}

private fun DrawScope.drawDiamond(x: Float, y: Float, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(x, y - size)
        lineTo(x + size, y)
        lineTo(x, y + size)
        lineTo(x - size, y)
        close()
    }
    drawPath(path, color)
}

// ----------------------------------------------------
// 11. SUI LOGO
// ----------------------------------------------------
private fun DrawScope.drawSuiLogo(cx: Float, cy: Float, w: Float, h: Float) {
    val suiBlue = Color(0xFF4CA2FF)
    val dropPath = Path().apply {
        moveTo(cx, cy - h * 0.25f)
        cubicTo(cx + w * 0.25f, cy - h * 0.05f, cx + w * 0.25f, cy + h * 0.22f, cx, cy + h * 0.22f)
        cubicTo(cx - w * 0.25f, cy + h * 0.22f, cx - w * 0.25f, cy - h * 0.05f, cx, cy - h * 0.25f)
        close()
    }
    drawPath(dropPath, suiBlue)
}

// ----------------------------------------------------
// 12. NEAR LOGO
// ----------------------------------------------------
private fun DrawScope.drawNearLogo(cx: Float, cy: Float, w: Float, h: Float) {
    val strokeW = w * 0.08f
    val path = Path().apply {
        moveTo(cx - w * 0.18f, cy + h * 0.22f)
        lineTo(cx - w * 0.18f, cy - h * 0.22f)
        lineTo(cx + w * 0.18f, cy + h * 0.22f)
        lineTo(cx + w * 0.18f, cy - h * 0.22f)
    }
    drawPath(path, Color.White, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

// ----------------------------------------------------
// 13. POLKADOT LOGO
// ----------------------------------------------------
private fun DrawScope.drawPolkadotLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFE6007A), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.12f, center = Offset(cx, cy - h * 0.08f))
    drawCircle(Color.White, radius = w * 0.07f, center = Offset(cx, cy + h * 0.16f))
}

// ----------------------------------------------------
// 14. POLYGON LOGO
// ----------------------------------------------------
private fun DrawScope.drawPolygonLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF8247E5), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    val path = Path().apply {
        moveTo(cx - w * 0.15f, cy)
        cubicTo(cx - w * 0.25f, cy - h * 0.16f, cx, cy - h * 0.16f, cx, cy)
        cubicTo(cx, cy + h * 0.16f, cx + w * 0.25f, cy + h * 0.16f, cx + w * 0.15f, cy)
    }
    drawPath(path, Color.White, style = Stroke(width = strokeW, cap = StrokeCap.Round))
}

// ----------------------------------------------------
// 15. TON LOGO
// ----------------------------------------------------
private fun DrawScope.drawTonLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0088CC), radius = w * 0.38f, center = Offset(cx, cy))
    val path = Path().apply {
        moveTo(cx, cy - h * 0.18f)
        lineTo(cx + w * 0.18f, cy - h * 0.02f)
        lineTo(cx, cy + h * 0.20f)
        lineTo(cx - w * 0.18f, cy - h * 0.02f)
        close()
    }
    drawPath(path, Color.White)
}

// ----------------------------------------------------
// 16. TRON LOGO
// ----------------------------------------------------
private fun DrawScope.drawTronLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFEF0027), radius = w * 0.38f, center = Offset(cx, cy))
    val path = Path().apply {
        moveTo(cx - w * 0.18f, cy - h * 0.16f)
        lineTo(cx + w * 0.20f, cy - h * 0.16f)
        lineTo(cx, cy + h * 0.22f)
        close()
    }
    drawPath(path, Color.White, style = Stroke(width = w * 0.05f, join = StrokeJoin.Round))
    drawLine(Color.White, Offset(cx - w * 0.18f, cy - h * 0.16f), Offset(cx, cy + h * 0.04f), strokeWidth = w * 0.04f)
}

// ----------------------------------------------------
// 17. UNISWAP LOGO
// ----------------------------------------------------
private fun DrawScope.drawUniswapLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFFF007A), radius = w * 0.38f, center = Offset(cx, cy))
    val path = Path().apply {
        moveTo(cx - w * 0.12f, cy + h * 0.15f)
        cubicTo(cx - w * 0.18f, cy - h * 0.15f, cx + w * 0.15f, cy - h * 0.22f, cx + w * 0.15f, cy - h * 0.22f)
        cubicTo(cx + w * 0.05f, cy, cx + w * 0.12f, cy + h * 0.15f, cx - w * 0.12f, cy + h * 0.15f)
        close()
    }
    drawPath(path, Color.White)
}

// ----------------------------------------------------
// 18. PEPE LOGO
// ----------------------------------------------------
private fun DrawScope.drawPepeLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF5BA349), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.08f, center = Offset(cx - w * 0.10f, cy - h * 0.06f))
    drawCircle(Color.White, radius = w * 0.08f, center = Offset(cx + w * 0.10f, cy - h * 0.06f))
    drawCircle(Color.Black, radius = w * 0.04f, center = Offset(cx - w * 0.08f, cy - h * 0.06f))
    drawCircle(Color.Black, radius = w * 0.04f, center = Offset(cx + w * 0.12f, cy - h * 0.06f))
    val smile = Path().apply {
        moveTo(cx - w * 0.14f, cy + h * 0.08f)
        cubicTo(cx, cy + h * 0.18f, cx, cy + h * 0.18f, cx + w * 0.14f, cy + h * 0.08f)
    }
    drawPath(smile, Color(0xFFC84332), style = Stroke(width = w * 0.04f, cap = StrokeCap.Round))
}

// ----------------------------------------------------
// 19. SHIBA LOGO
// ----------------------------------------------------
private fun DrawScope.drawShibaLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFFFA409), radius = w * 0.38f, center = Offset(cx, cy))
    val path = Path().apply {
        moveTo(cx - w * 0.18f, cy - h * 0.15f)
        lineTo(cx - w * 0.08f, cy)
        lineTo(cx, cy + h * 0.14f)
        lineTo(cx + w * 0.08f, cy)
        lineTo(cx + w * 0.18f, cy - h * 0.15f)
        lineTo(cx, cy - h * 0.08f)
        close()
    }
    drawPath(path, Color.White)
}

// ----------------------------------------------------
// 20. COSMOS (ATOM)
// ----------------------------------------------------
private fun DrawScope.drawCosmosLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF2E3148), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.07f, center = Offset(cx, cy))
    val r = w * 0.22f
    drawCircle(Color.White.copy(alpha = 0.85f), radius = r * 0.8f, center = Offset(cx, cy), style = Stroke(width = w * 0.04f))
}

// ----------------------------------------------------
// 21. ARBITRUM (ARB)
// ----------------------------------------------------
private fun DrawScope.drawArbitrumLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF28A0F0), radius = w * 0.38f, center = Offset(cx, cy))
    val path = Path().apply {
        moveTo(cx - w * 0.18f, cy + h * 0.15f)
        lineTo(cx, cy - h * 0.18f)
        lineTo(cx + w * 0.18f, cy + h * 0.15f)
        lineTo(cx + w * 0.10f, cy + h * 0.15f)
        lineTo(cx, cy - h * 0.05f)
        lineTo(cx - w * 0.10f, cy + h * 0.15f)
        close()
    }
    drawPath(path, Color.White)
}

// ----------------------------------------------------
// 22. OPTIMISM (OP)
// ----------------------------------------------------
private fun DrawScope.drawOptimismLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFFF0420), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.13f, center = Offset(cx - w * 0.08f, cy), style = Stroke(width = w * 0.05f))
    drawCircle(Color.White, radius = w * 0.13f, center = Offset(cx + w * 0.08f, cy), style = Stroke(width = w * 0.05f))
}

// ----------------------------------------------------
// 23. INJECTIVE (INJ)
// ----------------------------------------------------
private fun DrawScope.drawInjectiveLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF00B2FE), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    drawLine(Color.White, Offset(cx - w * 0.12f, cy - h * 0.18f), Offset(cx + w * 0.12f, cy + h * 0.18f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawCircle(Color.White, radius = w * 0.07f, center = Offset(cx - w * 0.10f, cy + h * 0.10f))
    drawCircle(Color.White, radius = w * 0.07f, center = Offset(cx + w * 0.10f, cy - h * 0.10f))
}

// ----------------------------------------------------
// 24. CELESTIA (TIA)
// ----------------------------------------------------
private fun DrawScope.drawCelestiaLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF7B2BF9), radius = w * 0.38f, center = Offset(cx, cy))
    val r = w * 0.18f
    for (i in 0..5) {
        val angle = Math.toRadians((i * 60).toDouble())
        val x = (cx + r * Math.cos(angle)).toFloat()
        val y = (cy + r * Math.sin(angle)).toFloat()
        drawCircle(Color.White, radius = w * 0.045f, center = Offset(x, y))
    }
}

// ----------------------------------------------------
// 25. RENDER (RENDER / RNDR)
// ----------------------------------------------------
private fun DrawScope.drawRenderLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFE51D24), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.16f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
    drawCircle(Color.White, radius = w * 0.06f, center = Offset(cx, cy))
}

// ----------------------------------------------------
// 26. FETCH.AI / ASI (FET)
// ----------------------------------------------------
private fun DrawScope.drawFetchLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF1B2342), radius = w * 0.38f, center = Offset(cx, cy))
    val r = w * 0.16f
    drawCircle(Color(0xFF13F3BC), radius = r, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
    drawCircle(Color.White, radius = w * 0.05f, center = Offset(cx, cy))
}

// ----------------------------------------------------
// 27. BITTENSOR (TAO)
// ----------------------------------------------------
private fun DrawScope.drawTaoLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFFFFFFF), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.08f
    drawLine(Color.Black, Offset(cx - w * 0.16f, cy - h * 0.14f), Offset(cx + w * 0.16f, cy - h * 0.14f), strokeWidth = strokeW, cap = StrokeCap.Square)
    drawLine(Color.Black, Offset(cx, cy - h * 0.14f), Offset(cx, cy + h * 0.18f), strokeWidth = strokeW, cap = StrokeCap.Square)
}

// ----------------------------------------------------
// 28. KASPA (KAS)
// ----------------------------------------------------
private fun DrawScope.drawKaspaLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF70C7BA), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    drawLine(Color.White, Offset(cx - w * 0.12f, cy - h * 0.18f), Offset(cx - w * 0.12f, cy + h * 0.18f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx - w * 0.12f, cy), Offset(cx + w * 0.14f, cy - h * 0.18f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx - w * 0.12f, cy), Offset(cx + w * 0.14f, cy + h * 0.18f), strokeWidth = strokeW, cap = StrokeCap.Round)
}

// ----------------------------------------------------
// 29. HEDERA (HBAR)
// ----------------------------------------------------
private fun DrawScope.drawHederaLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF222222), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.055f
    val lX = cx - w * 0.11f
    val rX = cx + w * 0.11f
    drawLine(Color.White, Offset(lX, cy - h * 0.18f), Offset(lX, cy + h * 0.18f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(rX, cy - h * 0.18f), Offset(rX, cy + h * 0.18f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(lX, cy - h * 0.05f), Offset(rX, cy - h * 0.05f), strokeWidth = strokeW * 0.8f)
    drawLine(Color.White, Offset(lX, cy + h * 0.05f), Offset(rX, cy + h * 0.05f), strokeWidth = strokeW * 0.8f)
}

// ----------------------------------------------------
// 30. STELLAR (XLM)
// ----------------------------------------------------
private fun DrawScope.drawStellarLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF14B6EB), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.05f
    drawLine(Color.White, Offset(cx - w * 0.16f, cy - h * 0.06f), Offset(cx + w * 0.16f, cy - h * 0.06f), strokeWidth = strokeW)
    drawLine(Color.White, Offset(cx - w * 0.16f, cy + h * 0.06f), Offset(cx + w * 0.16f, cy + h * 0.06f), strokeWidth = strokeW)
    drawCircle(Color.White, radius = w * 0.16f, center = Offset(cx, cy), style = Stroke(width = strokeW))
}

// ----------------------------------------------------
// 31. ALGORAND (ALGO)
// ----------------------------------------------------
private fun DrawScope.drawAlgorandLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF000000), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    drawLine(Color.White, Offset(cx - w * 0.14f, cy + h * 0.16f), Offset(cx + w * 0.05f, cy - h * 0.16f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx + w * 0.05f, cy - h * 0.16f), Offset(cx + w * 0.14f, cy + h * 0.16f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx - w * 0.05f, cy + h * 0.02f), Offset(cx + w * 0.10f, cy + h * 0.02f), strokeWidth = strokeW, cap = StrokeCap.Round)
}

// ----------------------------------------------------
// 32. INTERNET COMPUTER (ICP)
// ----------------------------------------------------
private fun DrawScope.drawIcpLogo(cx: Float, cy: Float, w: Float, h: Float) {
    val strokeW = w * 0.06f
    val path = Path().apply {
        moveTo(cx, cy)
        cubicTo(cx - w * 0.22f, cy - h * 0.14f, cx - w * 0.22f, cy + h * 0.14f, cx, cy)
        cubicTo(cx + w * 0.22f, cy - h * 0.14f, cx + w * 0.22f, cy + h * 0.14f, cx, cy)
    }
    drawPath(path, Color(0xFF29ABE2), style = Stroke(width = strokeW))
}

// ----------------------------------------------------
// 33. FILECOIN (FIL)
// ----------------------------------------------------
private fun DrawScope.drawFilecoinLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0090FF), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    drawLine(Color.White, Offset(cx - w * 0.10f, cy - h * 0.18f), Offset(cx + w * 0.14f, cy - h * 0.18f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx - w * 0.10f, cy - h * 0.18f), Offset(cx - w * 0.10f, cy + h * 0.18f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx - w * 0.10f, cy), Offset(cx + w * 0.08f, cy), strokeWidth = strokeW, cap = StrokeCap.Round)
}

// ----------------------------------------------------
// 34. AAVE
// ----------------------------------------------------
private fun DrawScope.drawAaveLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFB6509E), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    val path = Path().apply {
        moveTo(cx - w * 0.15f, cy + h * 0.16f)
        lineTo(cx, cy - h * 0.16f)
        lineTo(cx + w * 0.15f, cy + h * 0.16f)
    }
    drawPath(path, Color.White, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawLine(Color.White, Offset(cx - w * 0.08f, cy + h * 0.04f), Offset(cx + w * 0.08f, cy + h * 0.04f), strokeWidth = strokeW * 0.8f)
}

// ----------------------------------------------------
// 35. MAKER (MKR)
// ----------------------------------------------------
private fun DrawScope.drawMakerLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF1AAB9B), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    val path = Path().apply {
        moveTo(cx - w * 0.15f, cy + h * 0.16f)
        lineTo(cx - w * 0.15f, cy - h * 0.16f)
        lineTo(cx, cy + h * 0.02f)
        lineTo(cx + w * 0.15f, cy - h * 0.16f)
        lineTo(cx + w * 0.15f, cy + h * 0.16f)
    }
    drawPath(path, Color.White, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

// ----------------------------------------------------
// 36. STACKS (STX)
// ----------------------------------------------------
private fun DrawScope.drawStacksLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF5546FF), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    drawLine(Color.White, Offset(cx - w * 0.14f, cy - h * 0.14f), Offset(cx + w * 0.14f, cy + h * 0.14f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx + w * 0.14f, cy - h * 0.14f), Offset(cx - w * 0.14f, cy + h * 0.14f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx - w * 0.14f, cy), Offset(cx + w * 0.14f, cy), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
}

// ----------------------------------------------------
// 37. PYTH NETWORK
// ----------------------------------------------------
private fun DrawScope.drawPythLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF533483), radius = w * 0.38f, center = Offset(cx, cy))
    drawDiamond(cx, cy, w * 0.15f, Color.White)
}

// ----------------------------------------------------
// 38. JUPITER (JUP)
// ----------------------------------------------------
private fun DrawScope.drawJupiterLogo(cx: Float, cy: Float, w: Float, h: Float) {
    val jupGrad = Brush.horizontalGradient(listOf(Color(0xFFC7F284), Color(0xFF00BEA0)))
    drawCircle(jupGrad, radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color(0xFF08090D), radius = w * 0.14f, center = Offset(cx, cy))
}

// ----------------------------------------------------
// 39. DOGWIFHAT (WIF) & BONK
// ----------------------------------------------------
private fun DrawScope.drawWifLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFC29B72), radius = w * 0.38f, center = Offset(cx, cy))
    val hatPath = Path().apply {
        moveTo(cx - w * 0.18f, cy - h * 0.05f)
        lineTo(cx + w * 0.18f, cy - h * 0.05f)
        lineTo(cx + w * 0.10f, cy - h * 0.22f)
        lineTo(cx - w * 0.10f, cy - h * 0.22f)
        close()
    }
    drawPath(hatPath, Color(0xFFFF69B4))
}

private fun DrawScope.drawBonkLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFFF8E15), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.12f, center = Offset(cx, cy))
}

// ----------------------------------------------------
// POPCAT, ETHENA, EIGEN, MANTLE, STARKNET, ZKSYNC, LAYERZERO, WORMHOLE, CURVE
// ----------------------------------------------------
private fun DrawScope.drawPopcatLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFE84142), radius = w * 0.38f, center = Offset(cx, cy))
    val ears = Path().apply {
        moveTo(cx - w * 0.16f, cy - h * 0.05f)
        lineTo(cx - w * 0.16f, cy - h * 0.22f)
        lineTo(cx - w * 0.05f, cy - h * 0.10f)
        lineTo(cx + w * 0.05f, cy - h * 0.10f)
        lineTo(cx + w * 0.16f, cy - h * 0.22f)
        lineTo(cx + w * 0.16f, cy - h * 0.05f)
        close()
    }
    drawPath(ears, Color.White)
    drawCircle(Color(0xFF0F1218), radius = w * 0.12f, center = Offset(cx, cy + h * 0.06f))
}

private fun DrawScope.drawEthenaLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF1B1D23), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    val path = Path().apply {
        moveTo(cx + w * 0.14f, cy - h * 0.16f)
        lineTo(cx - w * 0.14f, cy - h * 0.16f)
        lineTo(cx - w * 0.14f, cy + h * 0.16f)
        lineTo(cx + w * 0.14f, cy + h * 0.16f)
    }
    drawPath(path, Color(0xFFC7F284), style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawLine(Color(0xFFC7F284), Offset(cx - w * 0.14f, cy), Offset(cx + w * 0.08f, cy), strokeWidth = strokeW, cap = StrokeCap.Round)
}

private fun DrawScope.drawEigenLayerLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF53389E), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.05f
    val r = w * 0.16f
    for (i in 0..2) {
        val yOffset = (i - 1) * h * 0.10f
        drawLine(Color.White, Offset(cx - r, cy + yOffset), Offset(cx + r, cy + yOffset), strokeWidth = strokeW, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawMantleLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF000000), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color(0xFF00FFC2), radius = w * 0.20f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
    drawCircle(Color(0xFF00FFC2), radius = w * 0.08f, center = Offset(cx, cy))
}

private fun DrawScope.drawStarknetLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0C0C4F), radius = w * 0.38f, center = Offset(cx, cy))
    val r = w * 0.20f
    for (i in 0..3) {
        val angle = Math.toRadians((i * 45).toDouble())
        val x1 = (cx + r * Math.cos(angle)).toFloat()
        val y1 = (cy + r * Math.sin(angle)).toFloat()
        val x2 = (cx - r * Math.cos(angle)).toFloat()
        val y2 = (cy - r * Math.sin(angle)).toFloat()
        drawLine(Color(0xFFEB5A28), Offset(x1, y1), Offset(x2, y2), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
    }
    drawCircle(Color.White, radius = w * 0.06f, center = Offset(cx, cy))
}

private fun DrawScope.drawZkSyncLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF000000), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    val path = Path().apply {
        moveTo(cx - w * 0.16f, cy - h * 0.16f)
        lineTo(cx + w * 0.16f, cy - h * 0.16f)
        lineTo(cx - w * 0.16f, cy + h * 0.16f)
        lineTo(cx + w * 0.16f, cy + h * 0.16f)
    }
    drawPath(path, Color(0xFF3366FF), style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawLayerZeroLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF000000), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.18f, center = Offset(cx, cy), style = Stroke(width = w * 0.06f))
}

private fun DrawScope.drawWormholeLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF05051F), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.055f
    val path = Path().apply {
        moveTo(cx - w * 0.18f, cy - h * 0.14f)
        lineTo(cx - w * 0.09f, cy + h * 0.16f)
        lineTo(cx, cy - h * 0.04f)
        lineTo(cx + w * 0.09f, cy + h * 0.16f)
        lineTo(cx + w * 0.18f, cy - h * 0.14f)
    }
    drawPath(path, Color(0xFFFF5C00), style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawCurveLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0038FF), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color(0xFFFF0055), radius = w * 0.22f, center = Offset(cx, cy + h * 0.08f), style = Stroke(width = w * 0.04f))
    drawCircle(Color(0xFFFFCC00), radius = w * 0.14f, center = Offset(cx, cy + h * 0.08f), style = Stroke(width = w * 0.04f))
}

private fun DrawScope.drawAkashLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFED3524), radius = w * 0.38f, center = Offset(cx, cy))
    val path = Path().apply {
        moveTo(cx, cy - h * 0.18f)
        lineTo(cx + w * 0.18f, cy + h * 0.16f)
        lineTo(cx - w * 0.18f, cy + h * 0.16f)
        close()
    }
    drawPath(path, Color.White, style = Stroke(width = w * 0.05f, join = StrokeJoin.Round))
}

private fun DrawScope.drawArweaveLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF222326), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    drawCircle(Color.White, radius = w * 0.12f, center = Offset(cx - w * 0.04f, cy), style = Stroke(width = strokeW))
    drawLine(Color.White, Offset(cx + w * 0.08f, cy - h * 0.14f), Offset(cx + w * 0.08f, cy + h * 0.14f), strokeWidth = strokeW, cap = StrokeCap.Round)
}

private fun DrawScope.drawHeliumLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0F1524), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color(0xFF00FFA3), radius = w * 0.20f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
    drawCircle(Color(0xFF00FFA3), radius = w * 0.08f, center = Offset(cx, cy))
}

private fun DrawScope.drawOndoLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0A0F1D), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color(0xFF4A72FF), radius = w * 0.20f, center = Offset(cx, cy), style = Stroke(width = w * 0.04f))
    drawCircle(Color(0xFF4A72FF), radius = w * 0.10f, center = Offset(cx, cy))
}

private fun DrawScope.drawPendleLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF091428), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.055f
    val path = Path().apply {
        moveTo(cx - w * 0.10f, cy + h * 0.16f)
        lineTo(cx - w * 0.10f, cy - h * 0.16f)
        cubicTo(cx + w * 0.18f, cy - h * 0.16f, cx + w * 0.18f, cy, cx - w * 0.10f, cy)
    }
    drawPath(path, Color(0xFF28E0B9), style = Stroke(width = strokeW, cap = StrokeCap.Round))
}

private fun DrawScope.drawSeiLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF9E1F36), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    val path = Path().apply {
        moveTo(cx + w * 0.12f, cy - h * 0.14f)
        cubicTo(cx - w * 0.14f, cy - h * 0.14f, cx - w * 0.14f, cy, cx, cy)
        cubicTo(cx + w * 0.14f, cy, cx + w * 0.14f, cy + h * 0.14f, cx - w * 0.12f, cy + h * 0.14f)
    }
    drawPath(path, Color.White, style = Stroke(width = strokeW, cap = StrokeCap.Round))
}

private fun DrawScope.drawWorldcoinLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF000000), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.20f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
    drawCircle(Color.White, radius = w * 0.08f, center = Offset(cx, cy))
}

private fun DrawScope.drawAptosLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF1E232A), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.045f
    val r = w * 0.16f
    for (i in 0..2) {
        val y = cy + (i - 1) * h * 0.10f
        drawLine(Color.White, Offset(cx - r, y), Offset(cx + r, y), strokeWidth = strokeW, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawFantomLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF1969FF), radius = w * 0.38f, center = Offset(cx, cy))
    val path = Path().apply {
        moveTo(cx, cy - h * 0.18f)
        lineTo(cx + w * 0.16f, cy)
        lineTo(cx, cy + h * 0.18f)
        lineTo(cx - w * 0.16f, cy)
        close()
    }
    drawPath(path, Color.White, style = Stroke(width = w * 0.05f, join = StrokeJoin.Round))
    drawLine(Color.White, Offset(cx - w * 0.16f, cy), Offset(cx + w * 0.16f, cy), strokeWidth = w * 0.04f)
}

private fun DrawScope.drawMoneroLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFFF6600), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    val path = Path().apply {
        moveTo(cx - w * 0.18f, cy + h * 0.16f)
        lineTo(cx - w * 0.18f, cy - h * 0.14f)
        lineTo(cx, cy + h * 0.04f)
        lineTo(cx + w * 0.18f, cy - h * 0.14f)
        lineTo(cx + w * 0.18f, cy + h * 0.16f)
    }
    drawPath(path, Color(0xFF2B2B2B), style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawGraphLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF6F3FF5), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.16f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
    drawCircle(Color.White, radius = w * 0.06f, center = Offset(cx - w * 0.04f, cy - h * 0.04f))
}

// ----------------------------------------------------
// PDF ASSET DIRECTORY LOGOS:
// ----------------------------------------------------
private fun DrawScope.drawFlareLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFE81E61), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    val path = Path().apply {
        moveTo(cx - w * 0.10f, cy + h * 0.16f)
        lineTo(cx - w * 0.10f, cy - h * 0.14f)
        cubicTo(cx + w * 0.14f, cy - h * 0.14f, cx + w * 0.14f, cy - h * 0.02f, cx - w * 0.10f, cy - h * 0.02f)
    }
    drawPath(path, Color.White, style = Stroke(width = strokeW, cap = StrokeCap.Round))
}

private fun DrawScope.drawRoseLogo(cx: Float, cy: Float, w: Float, h: Float) {
    val grad = Brush.sweepGradient(listOf(Color(0xFF0092F6), Color(0xFF0037B3), Color(0xFF0092F6)))
    drawCircle(grad, radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.16f, center = Offset(cx, cy), style = Stroke(width = w * 0.04f))
}

private fun DrawScope.drawMinaLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF1F1D2B), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.045f
    val grad = Brush.verticalGradient(listOf(Color(0xFFFF7A00), Color(0xFFFF0055)))
    for (i in 0..3) {
        val x = cx - w * 0.15f + i * w * 0.10f
        drawLine(grad, Offset(x, cy - h * 0.14f), Offset(x, cy + h * 0.14f), strokeWidth = strokeW, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawTezosLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF2C7DF7), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    drawLine(Color.White, Offset(cx - w * 0.12f, cy - h * 0.14f), Offset(cx + w * 0.12f, cy - h * 0.14f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx, cy - h * 0.14f), Offset(cx, cy + h * 0.16f), strokeWidth = strokeW, cap = StrokeCap.Round)
}

private fun DrawScope.drawKavaLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFFF433E), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    drawLine(Color.White, Offset(cx - w * 0.10f, cy - h * 0.16f), Offset(cx - w * 0.10f, cy + h * 0.16f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx - w * 0.10f, cy), Offset(cx + w * 0.12f, cy - h * 0.16f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx - w * 0.10f, cy), Offset(cx + w * 0.12f, cy + h * 0.16f), strokeWidth = strokeW, cap = StrokeCap.Round)
}

private fun DrawScope.drawCoreLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFFF9900), radius = w * 0.38f, center = Offset(cx, cy))
    drawDiamond(cx, cy, w * 0.16f, Color(0xFF1E1E1E))
    drawDiamond(cx, cy, w * 0.08f, Color(0xFFFF9900))
}

private fun DrawScope.drawBlastLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFFCFC03), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.065f
    val path = Path().apply {
        moveTo(cx - w * 0.10f, cy - h * 0.16f)
        lineTo(cx + w * 0.06f, cy - h * 0.16f)
        cubicTo(cx + w * 0.14f, cy - h * 0.16f, cx + w * 0.14f, cy - h * 0.02f, cx - w * 0.10f, cy - h * 0.02f)
        moveTo(cx - w * 0.10f, cy - h * 0.02f)
        lineTo(cx + w * 0.08f, cy - h * 0.02f)
        cubicTo(cx + w * 0.16f, cy - h * 0.02f, cx + w * 0.16f, cy + h * 0.16f, cx - w * 0.10f, cy + h * 0.16f)
    }
    drawLine(Color.Black, Offset(cx - w * 0.10f, cy - h * 0.16f), Offset(cx - w * 0.10f, cy + h * 0.16f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawPath(path, Color.Black, style = Stroke(width = strokeW, cap = StrokeCap.Round))
}

private fun DrawScope.drawZetaLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF006B3E), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    val path = Path().apply {
        moveTo(cx - w * 0.14f, cy - h * 0.14f)
        lineTo(cx + w * 0.14f, cy - h * 0.14f)
        lineTo(cx - w * 0.14f, cy + h * 0.14f)
        lineTo(cx + w * 0.14f, cy + h * 0.14f)
    }
    drawPath(path, Color.White, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawImxLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0D0D0D), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    drawLine(Color.White, Offset(cx - w * 0.14f, cy - h * 0.14f), Offset(cx + w * 0.14f, cy + h * 0.14f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx + w * 0.14f, cy - h * 0.14f), Offset(cx - w * 0.14f, cy + h * 0.14f), strokeWidth = strokeW, cap = StrokeCap.Round)
}

private fun DrawScope.drawMetisLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF00D2C4), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.14f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
}

private fun DrawScope.drawMantaLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0F172A), radius = w * 0.38f, center = Offset(cx, cy))
    val path = Path().apply {
        moveTo(cx, cy - h * 0.16f)
        lineTo(cx + w * 0.18f, cy)
        lineTo(cx, cy + h * 0.16f)
        lineTo(cx - w * 0.18f, cy)
        close()
    }
    drawPath(path, Color(0xFF00FFCC))
}

private fun DrawScope.drawTaikoLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFE81899), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.14f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
}

private fun DrawScope.drawThetaLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF2AB8E6), radius = w * 0.38f, center = Offset(cx, cy))
    drawDiamond(cx, cy, w * 0.14f, Color.White)
}

private fun DrawScope.drawOceanLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF141414), radius = w * 0.38f, center = Offset(cx, cy))
    for (i in -2..2) {
        drawCircle(Color.White, radius = w * 0.025f, center = Offset(cx + i * w * 0.06f, cy))
    }
}

private fun DrawScope.drawGlmLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF002244), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color(0xFF0088CC), radius = w * 0.14f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
}

private fun DrawScope.drawIoLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0A0A0A), radius = w * 0.38f, center = Offset(cx, cy))
    drawLine(Color.White, Offset(cx - w * 0.08f, cy - h * 0.14f), Offset(cx - w * 0.08f, cy + h * 0.14f), strokeWidth = w * 0.05f)
    drawCircle(Color.White, radius = w * 0.08f, center = Offset(cx + w * 0.08f, cy))
}

private fun DrawScope.drawAthLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF111111), radius = w * 0.38f, center = Offset(cx, cy))
    val path = Path().apply {
        moveTo(cx - w * 0.14f, cy + h * 0.16f)
        lineTo(cx, cy - h * 0.16f)
        lineTo(cx + w * 0.14f, cy + h * 0.16f)
    }
    drawPath(path, Color(0xFFC7F284), style = Stroke(width = w * 0.06f, cap = StrokeCap.Round))
}

private fun DrawScope.drawLdoLogo(cx: Float, cy: Float, w: Float, h: Float) {
    val grad = Brush.verticalGradient(listOf(Color(0xFF00A3FF), Color(0xFFFF7A00)))
    drawCircle(grad, radius = w * 0.38f, center = Offset(cx, cy))
}

private fun DrawScope.drawSnxLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF00D1FF), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color(0xFF0B0E14), radius = w * 0.14f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
}

private fun DrawScope.drawDydxLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF1E1E2F), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    drawLine(Color.White, Offset(cx - w * 0.14f, cy - h * 0.14f), Offset(cx + w * 0.14f, cy + h * 0.14f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx + w * 0.14f, cy - h * 0.14f), Offset(cx - w * 0.14f, cy + h * 0.14f), strokeWidth = strokeW, cap = StrokeCap.Round)
}

private fun DrawScope.drawGmxLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF2D42FC), radius = w * 0.38f, center = Offset(cx, cy))
    val path = Path().apply {
        moveTo(cx, cy - h * 0.16f)
        lineTo(cx + w * 0.16f, cy + h * 0.14f)
        lineTo(cx - w * 0.16f, cy + h * 0.14f)
        close()
    }
    drawPath(path, Color(0xFF00FFD1))
}

private fun DrawScope.drawRuneLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF00CC99), radius = w * 0.38f, center = Offset(cx, cy))
    drawLine(Color.White, Offset(cx - w * 0.12f, cy - h * 0.14f), Offset(cx + w * 0.12f, cy + h * 0.14f), strokeWidth = w * 0.06f, cap = StrokeCap.Round)
}

private fun DrawScope.drawRayLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF5A25F5), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.14f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
}

private fun DrawScope.drawAeroLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0052FF), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.15f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
}

private fun DrawScope.drawMorphoLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF1E3A8A), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color(0xFF60A5FA), radius = w * 0.16f, center = Offset(cx, cy))
}

private fun DrawScope.drawCompLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF00D395), radius = w * 0.38f, center = Offset(cx, cy))
    for (i in 0..2) {
        val y = cy - h * 0.10f + i * h * 0.10f
        drawLine(Color.White, Offset(cx - w * 0.14f, y), Offset(cx + w * 0.14f, y), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawCvxLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF18181B), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.16f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
}

private fun DrawScope.drawRplLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFFF6D00), radius = w * 0.38f, center = Offset(cx, cy))
    val path = Path().apply {
        moveTo(cx, cy - h * 0.16f)
        lineTo(cx + w * 0.12f, cy + h * 0.14f)
        lineTo(cx - w * 0.12f, cy + h * 0.14f)
        close()
    }
    drawPath(path, Color.White)
}

private fun DrawScope.drawFxsLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF000000), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.16f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
}

private fun DrawScope.drawCakeLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFD1884F), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.12f, center = Offset(cx, cy))
}

private fun DrawScope.drawFlokiLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFE89F12), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.14f, center = Offset(cx, cy))
}

private fun DrawScope.drawBrettLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF1D82FF), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.12f, center = Offset(cx, cy))
}

private fun DrawScope.drawNeiroLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFF59E0B), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.12f, center = Offset(cx, cy))
}

private fun DrawScope.drawDogsLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF000000), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.14f, center = Offset(cx, cy))
}

private fun DrawScope.drawMogLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF6366F1), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.12f, center = Offset(cx, cy))
}

private fun DrawScope.drawTurboLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF10B981), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.12f, center = Offset(cx, cy))
}

private fun DrawScope.drawJasmyLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFFF9500), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    drawLine(Color.White, Offset(cx - w * 0.08f, cy - h * 0.14f), Offset(cx + w * 0.08f, cy - h * 0.14f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(cx, cy - h * 0.14f), Offset(cx, cy + h * 0.10f), strokeWidth = strokeW, cap = StrokeCap.Round)
}

private fun DrawScope.drawIotaLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF131F37), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color.White, radius = w * 0.14f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
}

private fun DrawScope.drawGalaLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0F1B29), radius = w * 0.38f, center = Offset(cx, cy))
    drawDiamond(cx, cy, w * 0.16f, Color(0xFF00FFA3))
}

private fun DrawScope.drawAxsLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0055FF), radius = w * 0.38f, center = Offset(cx, cy))
    drawDiamond(cx, cy, w * 0.16f, Color.White)
}

private fun DrawScope.drawSandLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF0084FF), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    val path = Path().apply {
        moveTo(cx + w * 0.12f, cy - h * 0.12f)
        lineTo(cx - w * 0.12f, cy - h * 0.12f)
        lineTo(cx - w * 0.12f, cy)
        lineTo(cx + w * 0.12f, cy)
        lineTo(cx + w * 0.12f, cy + h * 0.12f)
        lineTo(cx - w * 0.12f, cy + h * 0.12f)
    }
    drawPath(path, Color.White, style = Stroke(width = strokeW, cap = StrokeCap.Square, join = StrokeJoin.Miter))
}

private fun DrawScope.drawManaLogo(cx: Float, cy: Float, w: Float, h: Float) {
    val grad = Brush.verticalGradient(listOf(Color(0xFFFF2D55), Color(0xFFFF9500)))
    drawCircle(grad, radius = w * 0.38f, center = Offset(cx, cy))
    val path = Path().apply {
        moveTo(cx, cy - h * 0.12f)
        lineTo(cx + w * 0.14f, cy + h * 0.12f)
        lineTo(cx - w * 0.14f, cy + h * 0.12f)
        close()
    }
    drawPath(path, Color.White)
}

private fun DrawScope.drawZilLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFF29CCC4), radius = w * 0.38f, center = Offset(cx, cy))
    val strokeW = w * 0.06f
    val path = Path().apply {
        moveTo(cx - w * 0.14f, cy - h * 0.14f)
        lineTo(cx + w * 0.14f, cy - h * 0.14f)
        lineTo(cx - w * 0.14f, cy + h * 0.14f)
        lineTo(cx + w * 0.14f, cy + h * 0.14f)
    }
    drawPath(path, Color.White, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawBlurLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFFF6600), radius = w * 0.38f, center = Offset(cx, cy))
    drawCircle(Color(0xFF000000), radius = w * 0.20f, center = Offset(cx, cy))
}

private fun DrawScope.drawChzLogo(cx: Float, cy: Float, w: Float, h: Float) {
    drawCircle(Color(0xFFCD0124), radius = w * 0.38f, center = Offset(cx, cy))
    val path = Path().apply {
        moveTo(cx - w * 0.12f, cy + h * 0.14f)
        lineTo(cx + w * 0.12f, cy - h * 0.14f)
    }
    drawPath(path, Color.White, style = Stroke(width = w * 0.06f, cap = StrokeCap.Round))
}

// ----------------------------------------------------
// GENERIC CRYPTO LOGO (Deterministic Color Monogram Badge)
// ----------------------------------------------------
private fun DrawScope.drawGenericCryptoLogo(sym: String, cx: Float, cy: Float, w: Float, h: Float) {
    val hash = Math.abs(sym.hashCode())
    val hues = listOf(
        Color(0xFF2563EB), Color(0xFF7C3AED), Color(0xFF059669),
        Color(0xFFD97706), Color(0xFFDC2626), Color(0xFF0891B2),
        Color(0xFF4F46E5), Color(0xFF0D9488), Color(0xFFBE185D)
    )
    val color = hues[hash % hues.size]

    drawCircle(
        color = color.copy(alpha = 0.35f),
        radius = w * 0.40f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = color,
        radius = w * 0.36f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5.dp.toPx())
    )
}
