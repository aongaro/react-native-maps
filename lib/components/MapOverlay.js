import React, { PropTypes, Component } from 'react';
import {
  View,
  StyleSheet,
  Animated,
} from 'react-native';

import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';
import decorateMapComponent, {
  SUPPORTED,
  USES_DEFAULT_IMPLEMENTATION,
} from './decorateMapComponent';

const viewConfig = {
  uiViewClassName: 'AIR<provider>MapOverlay',
  validAttributes: {
    image: true,
    bounds: true,
  },
};

const propTypes = {
  ...View.propTypes,
  // A custom image to be used as overlay.
  image: PropTypes.any.isRequired,
  // Top left and bottom right coordinates for the overlay
  bounds: PropTypes.arrayOf(PropTypes.array.isRequired).isRequired,
  //overlay visibility (defaults to true)
  visible: PropTypes.bool,
  //defaults to 0
  transparency: PropTypes.number,
  //defaults to 1
  zIndex: PropTypes.number,
  //defaults to 0
  rotation: PropTypes.number
};

class MapOverlay extends Component {

  render() {
    let image;

    image = resolveAssetSource(this.props.image) || {};
    image = image.uri;

    const AIRMapOverlay = this.getAirComponent();

    return (
      <AIRMapOverlay
        {...this.props}
        image={image}
        style={[styles.overlay, this.props.style]}
      />
    );
  }
}

MapOverlay.propTypes = propTypes;
MapOverlay.viewConfig = viewConfig;

const styles = StyleSheet.create({
  overlay: {
    position: 'absolute',
    backgroundColor: 'transparent',
  },
});

MapOverlay.Animated = Animated.createAnimatedComponent(MapOverlay);

module.exports = decorateMapComponent(MapOverlay, {
  componentType: 'Overlay',
  providers: {
    google: {
      ios: SUPPORTED,
      android: USES_DEFAULT_IMPLEMENTATION,
    },
  },
});
