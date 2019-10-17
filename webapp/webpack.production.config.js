var webpack = require('webpack');
var path = require('path');
var ExtractTextPlugin = require('extract-text-webpack-plugin'); //抽取CSS文件插件
var uglifyJsPlugin = webpack.optimize.UglifyJsPlugin;

module.exports = {
    entry: [
        path.resolve(__dirname, 'src/index.jsx')
    ],
    output: {
        path: 'dist',
        filename: '[name].bundle.js'
    },
    module: {
        loaders: [{
                test: /\.css$/,
                loader: ExtractTextPlugin.extract('style-loader', 'css-loader')
            }, //坑：不能用叹号链接，必须写成这种格式
            {
                test: /\.less$/,
                loader: ExtractTextPlugin.extract('style-loader', 'css-loader', 'less-loader')
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
        new uglifyJsPlugin({
            compress: {
                warnings: false
            }
        })
    ]
};