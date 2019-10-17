var webpack = require('webpack');
var path = require('path');
var ExtractTextPlugin = require('extract-text-webpack-plugin'); //抽取CSS文件插件
var OpenBrowserPlugin = require('open-browser-webpack-plugin'); //自动打开浏览器插件

module.exports = {

    // 配置服务器
    devServer: {
        historyApiFallback: true,
        hot: true,
        host: '0.0.0.0',
        inline: true,
        progress: true,
        contentBase: "./",
        port: 8000,
        disableHostCheck: true,
        proxy: {
            '/services/v1/*': {
                changeOrigin: true,
                //target: 'https://devradar.xwf-id.com',
                target: 'http://radar.pgmmer.top',
                //target: 'http://10.1.22.21:16580',
                secure: true
            }
        }
    },

    // 配置入口
    entry: [
        path.resolve(__dirname, 'src/index.jsx')
    ],
    output: {
        // path: 'dist',  //不写居然也没事，由于有服务器，生成不了静态文件，这也是一个坑
        publicPath: 'dist',
        filename: '[name].bundle.js'
    },
    module: {
        loaders: [{
                test: /\.css$/,
                loader: ExtractTextPlugin.extract('style-loader', 'css-loader')
            }, //坑：不能用叹号链接，必须写成这种格式
            {
                test: /\.less$/,
                loader: ExtractTextPlugin.extract('style-loader', 'css-loader!less-loader')
                    //loader: 'style!css!less'
            }, {
                test: /\.js[x]?$/,
                include: path.resolve(__dirname, 'src'),
                exclude: /node_modules/,
                loader: 'babel-loader'
            }, {
                test: /\.(png|jpg)$/,
                loader: 'url?limit=8192'
            }, {
                test: /\.(woff|woff2|eot|ttf|svg)(\?.*$|$)/,
                loader: 'url'
            }
        ]
    },
    resolve: {
        extensions: ['', '.js', '.jsx'],
    },
    plugins: [
        new webpack.optimize.CommonsChunkPlugin('common.js'),
        new ExtractTextPlugin("style.css"),
        new webpack.HotModuleReplacementPlugin(),
        new OpenBrowserPlugin({
            url: 'http://localhost:8000'
        })
    ]
};