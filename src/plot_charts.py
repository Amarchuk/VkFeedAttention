__author__ = 'user'
import matplotlib.pyplot as plt
from matplotlib import dates
import datetime
from matplotlib.ticker import MaxNLocator
import os


def reduce_data(date, data, by='h'):
    date2, data2 = [], []
    for i in range(0, len(date)):
        d = date[i]
        if by == 'h':
            d = d.replace(minute=0, second=0, microsecond=0)
            if date2.__contains__(d):
                data2[-1] = data2[-1] + data[i]
            else:
                date2.append(d)
                data2.append(data[i])
        if by == 'asis':
            d = d.replace(second=0, microsecond=0)
            if date2.__contains__(d):
                data2[-1] = data2[-1] + data[i]
            else:
                date2.append(d)
                data2.append(data[i])
    return date2, data2


if __name__ == "__main__":
    date, online, news = [], [], []
    days = {}

    datadir = '..\\data\\'

    for filename in os.listdir(datadir):
        if os.path.isfile(datadir + filename):
            with open(datadir + filename) as f:
                for line in f:
                    temp = line.split(", ", 6)
                    if int(temp[1]) != -1 and int(temp[2]) != -1:
                        date.append(datetime.datetime.fromtimestamp(int(temp[0]) / 1000.0))
                        online.append(int(temp[1]))
                        news.append(int(temp[2]))
                        day = date[-1].replace(hour=0, minute=0, second=0, microsecond=0)
                        if not days.keys().__contains__(day):
                            days[day] = 1
                        else:
                            days[day] += 1

    resolution = 'h' #asis | h (hour)

    f = plt.figure()

    ax1 = plt.subplot(311)
    lbound = 0
    set_date_to_zero = lambda l: l.replace(month=1, day=1)
    while lbound < date.__len__():
        day = date[lbound].replace(hour=0, minute=0, second=0, microsecond=0)
        ubound = lbound + days[day]
        ax1.plot(map(set_date_to_zero, date[lbound:ubound]), online[lbound:ubound], '-',
                 label="%s-%s (%s)" % (day.day, day.strftime("%b"), day.strftime("%a")))
        lbound = ubound
    ax1.grid()
    ax1.set_ylabel('Online friends')
    # hfmt = dates.DateFormatter('%m/%d-%H-%M')
    hfmt = dates.DateFormatter('%H-%M')
    if resolution == 'h':
        hfmt = dates.DateFormatter('%H')
    # ax1.xaxis.set_major_locator(dates.MinuteLocator())
    ax1.xaxis.set_major_formatter(hfmt)
    ax1.xaxis.set_major_locator(MaxNLocator(26))
    ax1.legend(loc='lower right')

    ax2 = plt.subplot(312)
    date2, news2 = reduce_data(date, news, by=resolution)
    width = 1./(24*60)
    if resolution == 'h':
        width = 1./24
    ax2.bar(date2, news2, width=width)
    ax2.set_ylabel('News in feed')
    ax2.xaxis.set_major_formatter(dates.DateFormatter('%m/%d-%H-%M'))
    ax2.xaxis_date()
    ax2.plot(date, online, '-', color='red')

    ax3 = plt.subplot(313, sharex=ax2)
    ind = 0
    li = 0
    attentions = []
    for minute in date:
        news_summ = 1
        attention = 0
        ind = date.index(minute)
        for next_minute in range(ind, len(news), 1):
            if news_summ > 100 or date[next_minute].minute > date[next_minute-1].minute + 5:
                attentions.append(attention)
                li = ind
                break
            else:
                news_summ += news[next_minute]
                attention += online[next_minute]*1./news_summ

    ax3.plot(date[0:li+1], attentions, '-', color='red')
    ax3.grid()
    plt.show()