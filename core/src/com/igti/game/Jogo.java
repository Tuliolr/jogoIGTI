package com.igti.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Jogo extends ApplicationAdapter {

	//Texturas
	private SpriteBatch batch;
	private Texture[] aviao;
	private Texture fundo;
	private Texture chao;
	private Texture obsBaixo;
	private Texture obsTopo;
	private Texture gameOver;
	//private Texture gol;

	//Formas para colisão
	private ShapeRenderer shapeRenderer;
	private Circle circuloAviao;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;

	//Atributos de configuração
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 0;
	private float posicaoPassaroY = 0;
	private float posicaoObsX;
	private float posicaoObsY;
	private float espacoEntreObs;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private int estadoJogo = 0;
	private float posicaoHorizontalAviao = 0;

	//Exibiçao de textos
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	//Configuração dos sons
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	//Objeto salvar pontuacao
	Preferences preferencias;

	//Objeto salvar camera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	
	@Override
	public void create () {
		inicializarTexturas();
		inicializarObjetos();

	}

	@Override
	public void render () {

	//Limpar frames
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

	verificarEstadoJogo();
	validarPontos();
	desenharTexturas();
	detectarColisoes();



	}

	private void desenharTexturas() {

		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		batch.draw(fundo,0,0, larguraDispositivo, alturaDispositivo);
		batch.draw(chao,0,0, larguraDispositivo, 100);
		batch.draw(aviao[ (int) variacao],50 + posicaoHorizontalAviao,posicaoPassaroY );
		batch.draw(obsBaixo, posicaoObsX, alturaDispositivo / 2 - obsBaixo.getHeight() - espacoEntreObs / 2 + posicaoObsY );
		//batch.draw(gol, posicaoObsX, alturaDispositivo /2 - gol.getHeight() - espacoEntreObs / 2 + posicaoObsY );
        batch.draw(obsTopo, posicaoObsX , alturaDispositivo / 2 + espacoEntreObs / 2 + posicaoObsY );
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo / 2, alturaDispositivo -110);

		if(estadoJogo == 2) {
			batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2, alturaDispositivo /2);
			textoReiniciar.draw(batch, "Toque para Reiniciar!", larguraDispositivo / 2 - 140, alturaDispositivo / 2 - gameOver.getHeight() / 2);
			textoMelhorPontuacao.draw(batch, "Seu record é: "+ pontuacaoMaxima +" pontos",larguraDispositivo / 2 - 140,  alturaDispositivo / 2 - gameOver.getHeight());
		}

		batch.end();

	}

	private void verificarEstadoJogo() {

		boolean toqueTela = Gdx.input.justTouched();

		if (estadoJogo == 0) {

			//Aplicar evento de click na tela
			if (toqueTela) {
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}

		} else if (estadoJogo == 1) {

			//Aplicar evento de click na tela
			if (toqueTela) {
				gravidade = -15;
				somVoando.play();

			}

			//Movimentar obstaculo
			posicaoObsX -= Gdx.graphics.getDeltaTime() * 300;
			if (posicaoObsX < -obsTopo.getWidth()) {
				posicaoObsX = larguraDispositivo;
				posicaoObsY = random.nextInt(400) - 200;
				passouCano = false;
			}

			//Aplica gravidade no aviao 400 -(-20) = 420
			if (posicaoPassaroY > 0 || toqueTela)
				posicaoPassaroY = posicaoPassaroY - gravidade;
			gravidade++;

		} else if (estadoJogo == 2) {
			/*
			//Aplica gravidade no pássaro 400 -(-20) = 420
			if (posicaoPassaroY > 0 || toqueTela)
				posicaoPassaroY = posicaoPassaroY - gravidade;
			gravidade++;
			*/

			if( pontos > pontuacaoMaxima) {
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima",pontuacaoMaxima);
			}



			posicaoHorizontalAviao -= Gdx.graphics.getDeltaTime() * 500;

			//Aplicar evento de click na tela
			if (toqueTela) {
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalAviao = 0;
				posicaoPassaroY = alturaDispositivo / 2;
				posicaoObsX = larguraDispositivo;

			}

		}
	}

	private void detectarColisoes(){

		circuloAviao.set(
				50 + posicaoHorizontalAviao + aviao[0].getWidth() /2 ,posicaoPassaroY + aviao[0].getHeight() / 2,aviao[0].getWidth() / 2
		);

		retanguloCanoBaixo.set(
				posicaoObsX, alturaDispositivo / 2 - obsBaixo.getHeight() - espacoEntreObs / 2 + posicaoObsY,
				obsBaixo.getWidth(),obsBaixo.getHeight()
		);

		retanguloCanoCima.set(
				posicaoObsX , alturaDispositivo / 2 + espacoEntreObs / 2 + posicaoObsY,
				obsTopo.getWidth(),obsTopo.getHeight()
		);

		boolean colidiuCanoCima = Intersector.overlaps(circuloAviao, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloAviao, retanguloCanoBaixo);

		if(colidiuCanoCima || colidiuCanoBaixo  ){
			if(estadoJogo == 1) {
				somColisao.play();
				estadoJogo = 2;

			}

		}

		/*
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.RED);

		shapeRenderer.circle(50 + aviao[0].getWidth() /2 ,posicaoPassaroY + aviao[0].getHeight() / 2,aviao[0].getWidth() / 2 );

		//topo
		shapeRenderer.rect(
				posicaoObsX, alturaDispositivo / 2 - obsBaixo.getHeight() - espacoEntreObs / 2 + posicaoObsY,
				obsBaixo.getWidth(),obsBaixo.getHeight()
		);

		shapeRenderer.rect(
				posicaoObsX , alturaDispositivo / 2 + espacoEntreObs / 2 + posicaoObsY,
				obsTopo.getWidth(),obsTopo.getHeight()
		);

		shapeRenderer.end();
		*/
	}

	public void validarPontos() {

		if(posicaoObsX < 50 - aviao[0].getWidth()) { //posicao do aviao
			if(!passouCano) {
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

        variacao += Gdx.graphics.getDeltaTime() * 70;
        //variacao da animação
        if (variacao > 3)
            variacao = 0;
	}

	private void inicializarTexturas(){

		aviao = new Texture[3];
		aviao[0] = new Texture("aviao1.png");
		aviao[1] = new Texture("aviao2.png");
		aviao[2] = new Texture("aviao3.png");

		fundo = new Texture("fundo2.png");
		//gol = new Texture("gol.png");
		obsBaixo = new Texture("cano_baixo_maior.png");
		obsTopo = new Texture("cano_topo_maior.png");
		chao = new Texture("chao.png");
		gameOver = new Texture("GameOver.png");
	}

	private void inicializarObjetos() {

		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoPassaroY = alturaDispositivo / 2;
		posicaoObsX = larguraDispositivo;
		espacoEntreObs = 350;

		//Configuraçoes dos textos
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);

		//Formas Geometricas para colisoes
		shapeRenderer = new ShapeRenderer();
		circuloAviao = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();

		//Inicializar sons
		somVoando = Gdx.audio.newSound( Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound( Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound( Gdx.files.internal("som_pontos.wav"));

		//Configura preferencias dos objetos
		preferencias = Gdx.app.getPreferences("record");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);

		//Configuração camera
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void dispose () {

	}
}
