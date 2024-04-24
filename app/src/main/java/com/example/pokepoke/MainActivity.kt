package com.example.pokepoke

import android.annotation.SuppressLint
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.pokepoke.data.Pokemon
import com.example.pokepoke.data.PokemonDetail
import com.example.pokepoke.data.PokemonListItem
import com.example.pokepoke.data.ScreenState
import com.example.pokepoke.network.ApiService
import com.example.pokepoke.ui.theme.PokePokeTheme
import com.example.pokepoke.ui.theme.background
import com.example.pokepoke.ui.theme.colorMap
import com.example.pokepoke.viewmodel.PokeViewModel
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.palette.BitmapPalette
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
                                route = ScreenState.Detail.name,
                            ) {
                                DetailView(this@composable)
                            }
                        }
//                        AnimatedContent(targetState = screenState, label = "", contentKey = {it.javaClass},transitionSpec = {
//                            if (initialState == ScreenState.Main) {
//                                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
//                            } else {
//                                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
//                            }
//                        }) {
//                            when (it) {
//                                ScreenState.Main -> PokemonLazyVerticalGrid(this)
//                                ScreenState.Detail -> DetailView(this)
//                            }
//                        }
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
            items(pokemonList.count()){
                if(!isLoading && it >= pokemonList.count()-1){
                    viewModel.loadPokemonList()
                }
                ListItem(data = pokemonList[it],animatedContentScope)
            }
        }
    }
    @OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
    @Composable
    fun SharedTransitionScope.ListItem(data: PokemonListItem,animatedContentScope: AnimatedVisibilityScope){

        var palette by remember {
            mutableStateOf<Palette?>(null)
        }

        Column(modifier = Modifier
            .fillMaxWidth(0.5f)
            .padding(10.dp)
            .combinedClickable(
                onClick = {
                    viewModel.loadDetail(data.number.toString())
                    //viewModel.navToDetail()
                    navController.navigate(ScreenState.Detail.name)

                },
            )
        ) {
            GlideImage(imageModel = data.imgUrl,
                requestOptions = { RequestOptions().encodeQuality(30).format(DecodeFormat.PREFER_RGB_565).centerCrop()
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

                    .sharedElement(
                            rememberSharedContentState(key = data.name),
                            animatedVisibilityScope = animatedContentScope
                        )

            )
            Text(
                text = String.format("No.%04d",data.number),
                color = Color.Gray,
                modifier = Modifier
                    .padding(top = 5.dp)
//                    .then(Modifier.sharedElement(
//                        rememberSharedContentState(key = "number${data.number}"),
//                        animatedVisibilityScope = animatedContentScope
//                    ).skipToLookaheadSize())
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
    fun SharedTransitionScope.DetailView(animatedContentScope: AnimatedVisibilityScope){

        var palette by remember {
            mutableStateOf<Palette?>(null)
        }
        val pokemonDetail by remember { viewModel.pokemonDetail }
        var expanded by remember { mutableStateOf(false) }

        Column(modifier = Modifier
            .fillMaxSize()
            .background(background)) {
            pokemonDetail?.let {
                val imgUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${it.id}.png"
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .background(
                        color = background,
                        shape = RectangleShape
                    )
                ) {

                    GlideImage(imageModel = imgUrl,
                        requestOptions = { RequestOptions().encodeQuality(30).format(DecodeFormat.PREFER_RGB_565).centerCrop()
                        },
                        bitmapPalette = BitmapPalette{
                            palette = it
                        },
                        contentDescription = "test",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(bottomEnd = 25.dp, bottomStart = 25.dp))
                            .background(
                                color = Color(palette?.dominantSwatch?.rgb ?: 255),
                                shape = RectangleShape
                            )

                                .sharedElement(
                                    rememberSharedContentState(key = it.name),
                                    animatedVisibilityScope = animatedContentScope
                                )

                    )
                    IconButton(modifier = Modifier.align(Alignment.TopStart),onClick = {
                        navController.popBackStack()
                        //viewModel.navToMain()
                        viewModel.pokemonDetail.value = null
                    }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back", tint = Color.White)

                    }
                    Text(text = String.format("No.%04d",it.id), color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, bottom = 10.dp, end = 7.dp)
//                        .then(Modifier.sharedElement(
//                            rememberSharedContentState(
//                                key = "number${it.id}"
//                            ), animatedContentScope
//                        ).skipToLookaheadSize())
                    )
                }

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
                    .wrapContentHeight(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Physical(type = "Weight", data = it.weight)
                    Physical(type = "Height", data = it.height)
                }
                Box(
                    modifier = Modifier
                        .background(Color.Gray)
                        .animateContentSize(animationSpec = tween(1000))
                        .height(20.dp)
                        .fillMaxWidth(if (expanded) 1f else 0f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            expanded = !expanded
                        }

                ) {
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
