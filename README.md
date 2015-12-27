# Vk.com Naive Feed attention maximization

**About:** This is a one-day project inspired by [post on Habrahabr](http://habrahabr.ru/post/183546/). The goal of work is to understand when better to post on vk.com my articles in terms of getting atteintion from my frineds. In other words, 'When to post?'

**New tech background**: set up and try to work with [Vk API](https://vk.com/dev/api_requests).

**Model**: Every minute during several days program gets amount of new posts in user feed via [newsfeed.get](https://vk.com/dev/newsfeed.get) and online friends number via [friends.get](https://vk.com/dev/friends.get). Then it calculates *«attention»* value based on collected data. *«Attention to some time»* defined as sum of rations of online friends at particular minute to all news posted betwen this minute and mine. This ratio calculated for every minute untill total count of posts after mine have not exceeded 100. Then I visualize all values as timeseries and make conclusions. I call this method naive because it based on rough assumptions that my friends have similar feeds; it does not recognize who is online exactly; doesn't count reposts etc. 

**How to use:** 
+ Get permissions to acess wall and friends `https://oauth.vk.com/authorize?client_id=5029615&scope=wall,friends,offline&redirect_uri=https://oauth.vk.com/blank.html&display=page&v=5.0&response_type=token` Copy obtained token and user_id into access_token.txt.
+ Clear /data folder and execute VkFeedAttention.java in order to start collect data, take a week of rest. 
+ Run plot_charts.py and analyze it (see below)

**Results:** This screenshot shows data of two weeks. First subplot display how number of online friends changes among the day. The second shows both number of friends and news on whole timeline. The last subplot demonstrates *«attention»* curve. It suggests that it is better to post between 12 p.m. and 1 a.m. This is not an unexpected insight because data clearly shows that slope of news number curve occurs earlier than slope of friends curve. So in spite of rough assumptions final result seems to be plausible.
![result plot](https://raw.githubusercontent.com/Amarchuk/VkFeedAttention/master/screenshot.png "Result plot")
