# Giphy Core SDK for Android


The **Giphy Core SDK** is a wrapper around [Giphy API](https://github.com/Giphy/GiphyAPI).

[![Build Status](https://travis-ci.com/Giphy/giphy-android-sdk-core.svg?token=3hcMqsQ6jx6dUF9GvWxR&branch=master)](https://travis-ci.com/Giphy/giphy-android-sdk-core)

[Giphy](https://www.giphy.com) is the best way to search, share, and discover GIFs on the Internet. Similar to the way other search engines work, the majority of our content comes from indexing based on the best and most popular GIFs and search terms across the web. We organize all those GIFs so you can find the good content easier and share it out through your social channels. We also feature some of our favorite GIF artists and work with brands to create and promote their original GIF content.

[![](https://media.giphy.com/media/5xaOcLOqNmWHaLeB14I/giphy.gif)]()

# Getting Started

### Supported Platforms

**Android minSdkVersion 14**

### Supported End-points

* [Search Gifs/Stickers](#search-endpoint)
* [Trending Gifs/Stickers](#trending-endpoint)
* [Translate Gifs/Stickers](#translate-endpoint)
* [Random Gifs/Stickers](#random-endpoint)
* [GIF by ID](#get-gif-by-id-endpoint)
* [GIFs by IDs](#get-gifs-by-ids-endpoint)
* [Categories for Gifs](#categories-endpoint)
* [Subcategories for Gifs](#subcategories-for-gifs)
* [Subcategory Content Endpoint](#subcategory-content-endpoint)
* [Query Suggestions](#translate-endpoint)


# Setup

### Maven dependency

Add the following dependency in the module ```build.gradle``` file:
```
compile('com.giphy.sdk:core:1.0.0@aar') {
    transitive=true
}
```

And add the following lines in the project ```build.gradle```:
```
repositories {
    maven {
        url  "https://giphy.bintray.com/giphy-sdk"
    }
}
```


### Local gradle dependency

Clone the sdk into the same folder as your app
```git
git clone https://github.com/Giphy/giphy-android-sdk-core.git
```
Include the sdk as a module by adding the following lines in ```settings.gradle``` file:
```gradle
include ':app', ':giphy-android-sdk-core'

project(':giphy-android-sdk-core').projectDir = new File(settingsDir, '../giphy-android-sdk-core/app')
```

Add the local module as a dependency in your ```build.gradle``` file:
```gradle
compile project(':giphy-android-sdk-core')
```

### Initialize Giphy SDK

```java
GPHApi client = new GPHApiClient("YOUR_API_KEY");
```

### Search Endpoint
Search all Giphy GIFs for a word or phrase. Punctuation will be stripped and ignored.

```java
/// Gif Search
client.search("cats", MediaType.gif, null, null, null, null, new CompletionHandler<ListMediaResponse>() {
    @Override
    public void onComplete(ListMediaResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                for (Media gif : result.getData()) {
                    Log.v("giphy", gif.getId());
                }
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
});

/// Sticker Search
client.search("cats", MediaType.sticker, null, null, null, null, new CompletionHandler<ListMediaResponse>() {
    @Override
    public void onComplete(ListMediaResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                for (Media gif : result.getData()) {
                    Log.v("giphy", gif.getId());
                }
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
});
```
### Trending Endpoint
Fetch GIFs currently trending online. Hand curated by the Giphy editorial team. The data returned mirrors the GIFs showcased on the [Giphy](https://www.giphy.com) homepage.

```java
/// Trending Gifs
client.trending(MediaType.gif, null, null, null, new CompletionHandler<ListMediaResponse>() {
    @Override
    public void onComplete(ListMediaResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                for (Media gif : result.getData()) {
                    Log.v("giphy", gif.getId());
                }
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
}

/// Trending Stickers
client.trending(MediaType.sticker, null, null, null, new CompletionHandler<ListMediaResponse>() {
    @Override
    public void onComplete(ListMediaResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                for (Media gif : result.getData()) {
                    Log.v("giphy", gif.getId());
                }
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
}
```

### Translate Endpoint
The translate API draws on search, but uses the Giphy "special sauce" to handle translating from one vocabulary to another. In this case, words and phrases to GIFs. Example implementations of translate can be found in the Giphy Slack, Hipchat, Wire, or Dasher integrations. Use a plus or url encode for phrases.

```java
/// Translate to a Gif
client.translate("hungry", MediaType.gif, null, null, new CompletionHandler<MediaResponse>() {
    @Override
    public void onComplete(MediaResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                Log.v("giphy", result.getData().getId());
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
});

/// Translate to a Sticker
client.translate("hungry", MediaType.sticker, null, null, new CompletionHandler<MediaResponse>() {
    @Override
    public void onComplete(MediaResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                Log.v("giphy", result.getData().getId());
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
});
```

### Random Endpoint
Returns a random GIF, limited by tag. Excluding the tag parameter will return a random GIF from the Giphy catalog.

```java
/// Random Gif
client.random("cats dogs", MediaType.gif, null, new CompletionHandler<MediaResponse>() {
    @Override
    public void onComplete(MediaResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                Log.v("giphy", result.getData().getId());
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
});

/// Random Sticker
client.random("cats dogs", MediaType.sticker, null, new CompletionHandler<MediaResponse>() {
    @Override
    public void onComplete(MediaResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                Log.v("giphy", result.getData().getId());
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
});
```

### Get GIF by ID Endpoint
Returns meta data about a GIF, by GIF id. In the below example, the GIF ID is "feqkVgjJpYtjy"

```java
/// Gif by Id
client.gifById("feqkVgjJpYtjy", new CompletionHandler<MediaResponse>() {
    @Override
    public void onComplete(MediaResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                Log.v("giphy", result.getData().getId());
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
});
```

### Get GIFs by IDs Endpoint
A multiget version of the get GIF by ID endpoint. In this case the IDs are feqkVgjJpYtjy and 7rzbxdu0ZEXLy.

```java
/// Gifs by Ids
List<String> gifIds = Arrays.asList("feqkVgjJpYtjy", "7rzbxdu0ZEXLy");

imp.gifsByIds(gifIds, new CompletionHandler<ListMediaResponse>() {
    @Override
    public void onComplete(ListMediaResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                for (Media gif : result.getData()) {
                    Log.v("giphy", gif.getId());
                }
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
});
```

### Categories Endpoint
Fetch Giphy categories 

```java
/// Categories
client.categoriesForGifs(null, null, null, new CompletionHandler<ListCategoryResponse>() {
    @Override
    public void onComplete(ListCategoryResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                for (Category category : result.getData()) {
                    Log.v("giphy", category.getName());
                }
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
}
```

### Subcategories Endpoint
Get Subcategories for GIFs given a category. You will need this subcategory object to pull GIFs for this category. 

```java
/// Categories
client.subCategoriesForGifs("actions", null, null, new CompletionHandler<ListCategoryResponse>() {
    @Override
    public void onComplete(ListCategoryResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                for (Category category : result.getData()) {
                    Log.v("giphy", category.getName());
                }
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
}
```

### Subcategory Content Endpoint
Fetch GIFs with a specific category & subcategory(tags)

```java
/// Gifs by Category
client.gifsByCategory("animals", "cats", null, null, new CompletionHandler<ListMediaResponse>() {
    @Override
    public void onComplete(ListMediaResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                for (Media gif : result.getData()) {
                    Log.v("giphy", gif.getId());
                }
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
}
```

### Term suggestions Endpoint
Get term suggestions given a search term, or a substring. 

```java
/// Term suggestions
client.termSuggestions("come", new CompletionHandler<ListTermSuggestionResponse>() {
    @Override
    public void onComplete(ListTermSuggestionResponse result, Throwable e) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                for (TermSuggestion term : result.getData()) {
                    Log.v("giphy", term.getTerm());
                }
            } else {
                Log.e("giphy error", "No results found");
            }
        }
    }
}
```

# CONTRIBUTING

Managing git repositories can be hard, so we've laid out a few simple guidelines to help keep things organized.

## Guidelines

1. Create a **Pull Request**; instead of pushing directly to `master`.

2. Give your branch a **descriptive name** like `dh-network-fix` instead of something ambiguous like `my-branch`.

3. Write a **descriptive summary** in the comment section on Github.

4. **Don't merge your own Pull Request**; send it to your teammate for review.

5. If you think something could be improved: **write a comment on the Pull Request** and send it to the author.

6. Make sure your branch is based off `master`, and not some other outdated branch.

7. **Don't reuse branches.** Once they're merged to `master` you should consider deleting them.

8. Prefer **squash** when doing a **Pull Request**, as it simplifies the commit history.
