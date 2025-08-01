/*
package com.keyboard.zomj.feature.gif;

import android.os.Handler;
import android.os.Message;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import com.android.inputmethod.BuildConfig;
import com.giphy.sdk.core.models.Category;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.core.models.enums.MediaType;
import com.giphy.sdk.core.network.api.CompletionHandler;
import com.giphy.sdk.core.network.api.GPHApi;
import com.giphy.sdk.core.network.api.GPHApiClient;
import com.giphy.sdk.core.network.response.ListCategoryResponse;
import com.giphy.sdk.core.network.response.ListMediaResponse;
import com.keyboard.zomj.App;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

import timber.log.Timber;

public class GifManager {
    public static final int MSG_REQUEST_CATEGORY_COMPLETE = 1;
    public static final int MSG_REQUEST_GIF_COMPLETE = 2;
    public GPHApi gphApi;
    public ArrayList<Category> categories = new ArrayList<>();
    public ArrayList<Media> media = new ArrayList<>();
    public ArrayList<Media> mediaTemp = new ArrayList<>();
    InputConnection inputConnection;
    Handler handler;
    private boolean isSearch = false;


    public GifManager() {
        gphApi = new GPHApiClient(BuildConfig.GIPHY_API);

    }

    public void setInputConnection(InputConnection inputConnection) {
        this.inputConnection = inputConnection;
    }


    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void loadCategories(){
        if(categories.size()>0){
            return;
        }
        gphApi.categoriesForGifs(null, null, null, new CompletionHandler<ListCategoryResponse>() {
            @Override
            public void onComplete(ListCategoryResponse listCategoryResponse, Throwable throwable) {
                if (listCategoryResponse != null) {
                    List<Category> data = listCategoryResponse.getData();
                    if (data != null) {
                        categories.clear();
                        categories.add(new Category("trending", "trending"));
                        categories.addAll(data);
                        if(handler!=null){
                            Message message = new Message();
                            message.what = MSG_REQUEST_CATEGORY_COMPLETE;
                            handler.sendMessage(message);
                        }

                    }
                }
            }

        });
    }
    public void getGiphyTrend() {
        isSearch = false;
        if(media.size()>0){
            return;
        }
        gphApi.trending(MediaType.gif, null, null, null, new CompletionHandler<ListMediaResponse>() {
            @Override
            public void onComplete(ListMediaResponse listMediaResponse, Throwable throwable) {
                if (listMediaResponse != null) {
                    List<Media> data = listMediaResponse.getData();
                    if (data != null && !isSearch) {
                        media.clear();
                        media.addAll(data);
                        mediaTemp.clear();
                        mediaTemp.addAll(data);
                        if(handler!=null){
                            Message message = new Message();
                            message.what = MSG_REQUEST_GIF_COMPLETE;
                            handler.sendMessage(message);
                        }

                    }
                }



            }
        });
    }
    public void getGiphyTrendNew() {
         Timber.d("ducNQs getGiphyTrendNew");
        gphApi.trending(MediaType.gif, null, null, null, new CompletionHandler<ListMediaResponse>() {
            @Override
            public void onComplete(ListMediaResponse listMediaResponse, Throwable throwable) {
                if (listMediaResponse != null) {
                    List<Media> data = listMediaResponse.getData();
                    if (data != null) {
                        media.clear();
                        media.addAll(data);
                        mediaTemp.clear();
                        mediaTemp.addAll(data);
                        if(handler!=null){
                            Message message = new Message();
                            message.what = MSG_REQUEST_GIF_COMPLETE;
                            handler.sendMessage(message);
                        }

                    }
                }
            }
        });
    }
    public void getGiphyByCategory(int position) {
        Timber.d("ducNQs getGiphyByCategory");
        Category category = categories.get(position);
        gphApi.gifsByCategory(category.getNameEncoded(), category.getNameEncoded(),
                null, null, null, null, new CompletionHandler<ListMediaResponse>() {
                    @Override
                    public void onComplete(ListMediaResponse listMediaResponse, Throwable throwable) {
                        if (listMediaResponse != null) {
                            List<Media> data = listMediaResponse.getData();
                            if (data != null) {
                                media.clear();
                                media.addAll(data);
                                mediaTemp.clear();
                                mediaTemp.addAll(data);
                                if(handler!=null){
                                    Message message = new Message();
                                    message.what = MSG_REQUEST_GIF_COMPLETE;
                                    handler.sendMessage(message);
                                }

                            }
                        }
                    }
                });
    }
    public void getGiphySearch(String textSearch) {
        media.clear();
        isSearch =true;
//        gifCategoryAdapter.setPos(-1);
        if(textSearch!=null && !textSearch.isEmpty()) {
            Message message = new Message();
            message.what = MSG_REQUEST_GIF_COMPLETE;
            handler.sendMessage(message);
            gphApi.search(textSearch, MediaType.gif, null, null, null, null, new CompletionHandler<ListMediaResponse>() {
                @Override
                public void onComplete(ListMediaResponse listMediaResponse, Throwable throwable) {
                    if (listMediaResponse != null) {
                        List<Media> data = listMediaResponse.getData();
                        if (data != null) {
                            media.clear();
                            media.addAll(data);
                            if (handler != null) {
                                Message message = new Message();
                                message.what = MSG_REQUEST_GIF_COMPLETE;
                                handler.sendMessage(message);
                            }
                        }
                    }
                }
            });
        }
    }
    public void changeCategory(){
        media.clear();
        media.addAll(mediaTemp);
        Message message = new Message();
        message.what = MSG_REQUEST_GIF_COMPLETE;
        handler.sendEmptyMessage(MSG_REQUEST_GIF_COMPLETE);
    }
}
*/
