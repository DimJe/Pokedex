package com.example.pokepoke

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.pokepoke.data.PokemonListItem
import com.example.pokepoke.data.ScreenState
import com.example.pokepoke.data.toStatData
import com.example.pokepoke.ui.theme.PokePokeTheme
import com.example.pokepoke.ui.theme.background
import com.example.pokepoke.ui.theme.colorMap
import com.example.pokepoke.viewmodel.PokeViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.palette.PalettePlugin
import com.skydoves.landscapist.palette.rememberPaletteState
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val viewModel: PokeViewModel by viewModels()
    private lateinit var navController: NavHostController
    @OptIn(ExperimentalSharedTransitionApi::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        viewModel.loadPokemonList()
        setContent {
            PokePokeTheme {
                navController = rememberNavController()
                Scaffold()
                {
                    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            startDestination = ScreenState.Main.name,
                            navController = navController,
                        ) {
                            composable(route = ScreenState.Main.name) {
                                PokemonLazyVerticalGrid(this@composable)
                            }
                            composable(
                                route = "${ScreenState.Detail.name}/{color}/{index}",
                                arguments = listOf(navArgument("color"){ type = NavType.IntType }, navArgument("index"){type = NavType.IntType})
                            ) {
                                DetailView(it.arguments?.getInt("color",255)!!,it.arguments?.getInt("index",0)!!,this@composable)
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    fun SharedTransitionScope.PokemonLazyVerticalGrid(animatedContentScope: AnimatedVisibilityScope){

        val pokemonList by remember { viewModel.pokemonList }
        //val endReached by remember { viewModel.endReached }
        //val loadError by remember { viewModel.loadError }
        val isLoading by remember { viewModel.isLoading }

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize(),
            columns = GridCells.Fixed(2)
        ) {
            item(span = { GridItemSpan(this.maxLineSpan)}) {
                MyTopAppBar(name = stringResource(id = R.string.app_name))
            }
            itemsIndexed(items = pokemonList,key = {_,pokemon -> pokemon.name}){index,pokemon ->
                if(!isLoading && index >= pokemonList.count()-1){
                    viewModel.loadPokemonList()
                }
                ListItem(data = pokemon,animatedContentScope)
            }
        }
    }
    @OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
    @Composable
    fun SharedTransitionScope.ListItem(data: PokemonListItem,animatedContentScope: AnimatedVisibilityScope){

        var palette by rememberPaletteState(value = null)

        Column(modifier = Modifier
            .padding(10.dp)
            .combinedClickable(
                onClick = {
                    viewModel.loadDetail(data.number.toString())
                    navController.navigate("${ScreenState.Detail.name}/${palette?.dominantSwatch?.rgb}/${data.number}")

                },
            )
        ) {
            GlideImage(imageModel = {data.imgUrl},
                requestOptions = {RequestOptions().encodeQuality(30).format(DecodeFormat.PREFER_RGB_565).diskCacheStrategy(DiskCacheStrategy.ALL)
                },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                ),
                component = rememberImageComponent {
                    +PalettePlugin{ palette = it}
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        color = Color(palette?.dominantSwatch?.rgb ?: 255),
                        shape = RectangleShape
                    )
                    .sharedElement(
                        rememberSharedContentState(key = "image-${data.number}"),
                        animatedVisibilityScope = animatedContentScope
                    )

            )
            Text(
                text = String.format("No.%04d",data.number),
                color = Color.Gray,
                modifier = Modifier
                    .padding(top = 5.dp)
            )
            Text(
                text = data.name.replaceFirstChar{it.uppercase()},
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                modifier = Modifier
                    .padding(vertical = 5.dp)
            )
        }
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    fun SharedTransitionScope.DetailView(color:Int,index:Int,animatedContentScope: AnimatedVisibilityScope){


        val pokemonDetail by remember { viewModel.pokemonDetail }
        Column(modifier = Modifier
            .fillMaxSize()
            .background(background)) {
            val imgUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${index}.png"
            Box(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .background(
                    color = background,
                    shape = RectangleShape
                )
            ) {
                //기존엔 viewmodel에서 상세 정보 조회 후 이미지가 그려졌기에 가끔 sharedTransition이 작동하지 않았다고 생각함
                //
                GlideImage(imageModel = { imgUrl },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(bottomEnd = 25.dp, bottomStart = 25.dp))
                        .background(
                            color = Color(color),
                            shape = RectangleShape
                        )
                        .sharedElement(
                            rememberSharedContentState(key = "image-${index}"),
                            animatedVisibilityScope = animatedContentScope
                        )

                )
                IconButton(modifier = Modifier.align(Alignment.TopStart),onClick = {
                    navController.navigateUp()
                    viewModel.pokemonDetail.value = null
                }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back", tint = Color.White)

                }
                Text(text = String.format("No.%04d",index), color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, bottom = 10.dp, end = 7.dp))
            }

            pokemonDetail?.let {

                Text(text = it.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 27.sp, modifier = Modifier
                    .padding(top = 20.dp, bottom = 10.dp)
                    .align(Alignment.CenterHorizontally)
                    )
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .wrapContentHeight(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center){
                    it.types.forEach {
                        TypeBox(text = it.type.name)
                    }
                }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Physical(type = "Weight", data = it.weight)
                    Physical(type = "Height", data = it.height)
                }
                Column {
                    it.toStatData().forEach {stats ->
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            Text(modifier = Modifier
                                .fillMaxWidth(0.2f)
                                .padding(start = 10.dp),text = stats.statsName, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
                            PokeProgressBar(baseStat = stats.stats, barColor = stats.color)
                        }
                        //PokeProgressBar(typeName = stats.stat.name, baseStat = stats.baseStat)
                        Spacer(modifier = Modifier.height(15.dp))
                    }
                }

            }
        }
    }
}
@Composable
fun Physical(type: String, data: Int){

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    )
    {
        if(type == "Weight"){
            Text(
                text = String.format("%.1f KG",data.toFloat().div(10)),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
        else{
            Text(
                text = String.format("%.1f M",data.toFloat().div(10)),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
        Text(modifier = Modifier
            .wrapContentWidth()
            .padding(vertical = 5.dp),text = type, color = Color.LightGray, fontSize = 15.sp,textAlign = TextAlign.Center)
    }

}
@Composable
fun TypeBox(text:String){
    Box(modifier = Modifier
        .wrapContentHeight()
        .padding(horizontal = 10.dp)
        .clip(CircleShape)
        .background(colorMap[text] ?: Color(0xFF9DC1B7))){
        Text(text = text.replaceFirstChar{it.uppercase()},
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(vertical = 5.dp, horizontal = 25.dp),
            style = TextStyle(textAlign = TextAlign.Justify)
        )
    }
}
@Composable
fun PokeProgressBar(baseStat: Int,barColor: Color){
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp.value
    val isLocalInspectionMode = LocalInspectionMode.current
    var progressWidth by remember {
        mutableFloatStateOf(
            if (isLocalInspectionMode) {
                screenWidth
            } else {
                0f
            },
        )
    }
    Box(
        modifier = Modifier
            .padding(end = 20.dp)
            .fillMaxWidth()
            .height(18.dp)
            .onSizeChanged { progressWidth = it.width * (baseStat / 300f) }
            .background(
                color = Color.White,
                shape = RoundedCornerShape(64.dp),
            )
            .clip(RoundedCornerShape(64.dp))
        ,
        contentAlignment = Alignment.Center
    ) {
        var textWidth by remember { mutableIntStateOf(0) }
        val threshold = 16
        //텍스트가 들어갈 수 있는 최소 자리가 있는지?
        val isInner by remember(
            progressWidth,
            textWidth,
        ) { mutableStateOf(progressWidth > (textWidth + threshold * 2)) }

        val animation: Float by animateFloatAsState(
            targetValue = if (progressWidth == 0f) 0f else 1f,
            // Configure the animation duration and easing.
            animationSpec = tween(durationMillis = 950, easing = LinearOutSlowInEasing),
            label = "",
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(
                    progressWidth
                        .toInt()
                        .pxToDp() * animation,
                )
                .height(18.dp)
                .padding(end = 10.dp)
                .background(
                    color = barColor,
                    shape = RoundedCornerShape(64.dp),
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isInner) {
                Text(
                    modifier = Modifier
                        .onSizeChanged { textWidth = it.width }
                        .align(Alignment.CenterEnd)
                        .padding(end = (threshold * 2).pxToDp()),
                    text = "$baseStat/300",
                    fontSize = 12.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!isInner) {
            Text(
                modifier = Modifier
                    .fillMaxHeight()
                    .onSizeChanged { textWidth = it.width }
                    .align(Alignment.CenterStart)
                    .padding(
                        start = progressWidth
                            .toInt()
                            .pxToDp() + threshold.pxToDp(),
                    ),
                text = "$baseStat/300",
                fontSize = 12.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }

    }
}
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MyTopAppBar(name: String){

    TopAppBar (
        title = { Text(text = name, color = Color.Black,modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .wrapContentSize(
                Alignment.Center
            )
        ) }, backgroundColor = Color.White,
    )
}
@Composable
fun Int.pxToDp(): Dp = with(LocalDensity.current) { this@pxToDp.toDp() }