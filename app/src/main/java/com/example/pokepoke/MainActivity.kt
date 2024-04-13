package com.example.pokepoke

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.google.accompanist.navigation.animation.composable
import androidx.navigation.navArgument
import androidx.palette.graphics.Palette
import com.google.accompanist.navigation.animation.navigation
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.pokepoke.data.Pokemon
import com.example.pokepoke.data.PokemonDetail
import com.example.pokepoke.data.PokemonListItem
import com.example.pokepoke.data.ScreenState
import com.example.pokepoke.network.ApiService
import com.example.pokepoke.ui.theme.PokePokeTheme
import com.example.pokepoke.viewmodel.PokeViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.palette.BitmapPalette
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

//url로 이미지 가져오기
//Image(painter = rememberImagePainter(data = memo.imageUrl), contentDescription = "대표이미지")
//미리 다음 이미지 캐시해두기
//기존 livedata에 새로 얻어온 20개의 데이터를 추가하는 과정이 리스트 크기가 커질수록 오래걸림
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    var TAG = "tag"
    var index = 0
    val viewModel: PokeViewModel by viewModels()
    lateinit var navController: NavHostController
    @OptIn(ExperimentalAnimationApi::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        viewModel.loadPokemonList()
        setContent {
            PokePokeTheme {
                navController = rememberAnimatedNavController()
                Scaffold(
                ) {

                    AnimatedNavHost(
                        navController = navController,
                        startDestination = ScreenState.Main.name,
                        //enterTransition = { scaleIn() },
                        exitTransition = { ExitTransition.None },
                        popExitTransition = { ExitTransition.None}
                    ){
                        composable(route = ScreenState.Main.name,){
                            PokemonLazyVerticalGrid()
                        }
                        composable(
                            enterTransition = {slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))},
                            route = "${ScreenState.Detail.name}/{data}/{x}/{y}",
                            arguments = listOf(navArgument("data",
                            ) {
                            type = NavType.StringType
                        })){
                            val x = it.arguments?.getString("x").toString().toFloat()
                            val y = it.arguments?.getString("y").toString().toFloat()
                            DetailView(it.arguments?.getString("data")!!)
                        }
                    }
                }
            }
        }
    }
    suspend fun getDetailList(urlList: List<Pokemon>): ArrayList<PokemonDetail>{
        val list = arrayListOf<PokemonDetail>()

        urlList.forEach {
            val result = ApiService.instance.getPokemonDetail(it.url)
            if (result.isSuccessful) list.add(result.body()!!)
        }
        Timber.d("end")
        return list
    }

    @Composable
    fun PokemonLazyVerticalGrid(){

        val pokemonList by remember { viewModel.pokemonList }
        val endReached by remember { viewModel.endReached }
        val loadError by remember { viewModel.loadError }
        val isLoading by remember { viewModel.isLoading }

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize(),
            columns = GridCells.Fixed(2)
        ) {
            item(span = { GridItemSpan(this.maxLineSpan)}) {
                MyTopAppBar(name = stringResource(id = R.string.app_name))
            }
            items(pokemonList.count()){
                if(!isLoading && it >= pokemonList.count()-1){
                    viewModel.loadPokemonList()
                }
                ListItem(data = pokemonList[it])
            }
        }
    }
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ListItem(data: PokemonListItem){

        var palette by remember {
            mutableStateOf<Palette?>(null)
        }
        var rotate by remember {
            mutableStateOf(false)
        }
        val rotation by animateFloatAsState(
            targetValue = if(rotate) 360f else 0f,
            animationSpec = tween(500),
            label = ""
        )
        var itemPosition by remember {
            mutableStateOf(Offset.Zero)
        }
        //리스트 나오는데 7-8초
        Column(modifier = Modifier
            .fillMaxWidth(0.5f)
            .padding(10.dp)
            .combinedClickable(
                onClick = {
                    navController.navigate("${ScreenState.Detail.name}/${data.number}/${itemPosition.x}/${itemPosition.y}") {
                    }
                },
                onLongClick = {
                    Timber.e("Long Click")
                    rotate = !rotate
                }
            )
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 7 * density
            }
            .onGloballyPositioned {
                itemPosition = it.localToWindow(Offset.Zero)
            }
        ) {
            GlideImage(imageModel = data.imgUrl,
                requestOptions = { RequestOptions().encodeQuality(30).format(DecodeFormat.PREFER_RGB_565).onlyRetrieveFromCache(true).centerCrop()
                },
                contentDescription = "test",
                bitmapPalette = BitmapPalette{
                    palette = it
                },
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        color = Color(palette?.dominantSwatch?.rgb ?: 255),
                        shape = RectangleShape
                    )
            )
            Text(text = String.format("No.%04d",data.number), color = Color.Gray, modifier = Modifier.padding(top = 5.dp))
            Text(text = data.name.replaceFirstChar{it.uppercase()}, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 17.sp,modifier = Modifier.padding(vertical = 5.dp))
        }
    }

    @Composable
    fun DetailView(dataJson : String){
        Timber.e("detail = $dataJson")
        val imgUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${dataJson}.png"
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .background(
                    color = Color.White,//colorMap[choiceData!!.types[0].type.name] ?: Color.White,
                    shape = RectangleShape
                )
            ) {
                GlideImage(imageModel = imgUrl,
                    requestOptions = { RequestOptions().encodeQuality(30).format(DecodeFormat.PREFER_RGB_565).onlyRetrieveFromCache(true).centerCrop()
                    },
                    contentDescription = "test",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
            }
        }
    }
}

@Composable
fun TypeBox(text:String,modifier: Modifier){
    Box(modifier = modifier){
        Text(text = text.replaceFirstChar{it.uppercase()},color = Color.White, textAlign = TextAlign.Center,modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
            style = TextStyle(textAlign = TextAlign.Justify)
        )
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
