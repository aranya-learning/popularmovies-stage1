package com.pushpal.popularmoviesstage1.activity;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pushpal.popularmoviesstage1.R;
import com.pushpal.popularmoviesstage1.database.MovieDatabase;
import com.pushpal.popularmoviesstage1.model.Movie;
import com.pushpal.popularmoviesstage1.utilities.Constants;
import com.pushpal.popularmoviesstage1.utilities.DateUtil;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = DetailsActivity.class.getSimpleName();

    @BindView(R.id.iv_movie_poster)
    ImageView moviePoster;
    @BindView(R.id.tv_movie_title)
    TextView movieTitle;
    @BindView(R.id.tv_movie_release_date)
    TextView movieReleaseDate;
    @BindView(R.id.tv_movie_language)
    TextView movieLanguage;
    @BindView(R.id.tv_vote_average)
    TextView movieVoteAverage;
    @BindView(R.id.tv_vote_count)
    TextView movieVoteCount;
    @BindView(R.id.tv_overview)
    TextView movieOverview;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btn_favourite)
    ShineButton likeButton;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    ActionBar actionBar;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this);
        context = this;

        supportPostponeEnterTransition();
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        Movie movie = null;
        if (extras != null)
            movie = extras.getParcelable(MainActivity.EXTRA_MOVIE_ITEM);

        if (movie != null) {
            movieTitle.setText(movie.getTitle());
            movieReleaseDate.setText(DateUtil.getFormattedDate(movie.getReleaseDate()));
            movieLanguage.setText(getLanguage(movie.getOriginalLanguage()));
            movieVoteAverage.setText(String.valueOf(movie.getVoteAverage()));
            String voteCount = String.valueOf(movie.getVoteCount()) + " " + getString(R.string.votes);
            movieVoteCount.setText(voteCount);
            movieOverview.setText(String.valueOf(movie.getOverview()));
            collapsingToolbarLayout.setTitle(movie.getTitle());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String imageTransitionName = extras.getString(MainActivity.EXTRA_MOVIE_IMAGE_TRANSITION_NAME);
                moviePoster.setTransitionName(imageTransitionName);
            }

            String imageURL = Constants.IMAGE_BASE_URL
                    + Constants.IMAGE_SIZE_342
                    + movie.getPosterPath();
            Picasso.with(this)
                    .load(imageURL)
                    .into(moviePoster, new Callback() {
                        @Override
                        public void onSuccess() {
                            supportStartPostponedEnterTransition();
                        }

                        @Override
                        public void onError() {
                            supportStartPostponedEnterTransition();
                        }
                    });

            try {
                URL url = new URL(imageURL);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@NonNull Palette palette) {
                        int mutedColor = palette.getMutedColor(R.attr.colorPrimary);
                        collapsingToolbarLayout.setContentScrimColor(mutedColor);
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            if (isFavourite(movie)) {
                likeButton.setChecked(true);
            }

            final Movie finalMovie = movie;
            likeButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(View view, boolean checked) {
                    if (checked)
                        new AddMovieToFavouritesAsync().execute(finalMovie);
                    else
                        new RemoveMovieFromFavouritesAsync().execute(finalMovie);
                }
            });
        }
    }

    private String getLanguage(String languageAbbr) {
        return MainActivity.languageMap.get(languageAbbr);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean isFavourite(Movie movie) {
        boolean isFav = false;
        if (MainActivity.favMovies != null) {
            for (Movie favMovie : MainActivity.favMovies) {
                if (movie.getId().equals(favMovie.getId())) {
                    isFav = true;
                    break;
                }
            }
        }

        return isFav;
    }

    private class AddMovieToFavouritesAsync extends AsyncTask<Movie, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Movie... movies) {

            try {
                MovieDatabase.getInstance(context)
                        .getMovieDao()
                        .insertMovie(movies[0]);
            } catch (SQLiteConstraintException e) {
                Log.e(TAG, e.getMessage());

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Toast.makeText(context, "Added to favourites", Toast.LENGTH_SHORT).show();
        }
    }

    private class RemoveMovieFromFavouritesAsync extends AsyncTask<Movie, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Movie... movies) {

            MovieDatabase.getInstance(context)
                    .getMovieDao()
                    .deleteMovie(movies[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Toast.makeText(context, "Removed from favourites", Toast.LENGTH_SHORT).show();
        }
    }
}